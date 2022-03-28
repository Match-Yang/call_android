package im.zego.callsdk.service;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoCommand;
import im.zego.callsdk.listener.ZegoListenerUpdater;
import im.zego.callsdk.model.DatabaseCall;
import im.zego.callsdk.model.DatabaseCall.DatabaseCallUser;
import im.zego.callsdk.model.DatabaseCall.Status;
import im.zego.callsdk.model.DatabaseUser;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ZegoFirebaseManager implements ZegoRequestProtocol {

    private ZegoListenerUpdater updater;
    private static final String TAG = "ZegoFirebaseManager";
    private List<ZegoUserInfo> onlineUserList = new ArrayList();
    private Map<String, ValueEventListener> callmap = new HashMap<>();

    public ZegoFirebaseManager() {
        updater = ZegoListenerManager.getInstance();
        initConnectListener();
        listenUserOnline();
    }

    @Override
    public void request(String path, Map<String, Object> parameter, ZegoRequestCallback callback) {
        if (ZegoCommand.LOGIN.equals(path)) {
            String authToken = (String) parameter.get("authToken");
            firebaseAuthWithGoogle(authToken, callback);
        } else if (ZegoCommand.LOGOUT.equals(path)) {
            signOutFirebaseAuth(parameter, callback);
        } else if (ZegoCommand.GET_USER_LIST.equals(path)) {
            queryDatabaseForOnlineUser(callback);
        } else if (ZegoCommand.START_CALL.equals(path)) {
            startCallUser(parameter, callback);
        } else if (ZegoCommand.END_CALL.equals(path)) {
            endCallUser(parameter, callback);
        } else if (ZegoCommand.ACCEPT_CALL.equals(path)) {
            acceptUserCall(parameter, callback);
        } else if (ZegoCommand.DECLINE_CALL.equals(path)) {
            declineUserCall(parameter, callback);
        } else if (ZegoCommand.CANCEL_CALL.equals(path)) {
            cancelUserCall(parameter, callback);
        } else if (ZegoCommand.Listener_CALL.equals(path)) {
            String callID = (String) parameter.get("callID");
            addCallListener(callID);
        }
    }

    private void startCallUser(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "startCallUser() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        ZegoCallType callType = (ZegoCallType) parameter.get("callType");
        String selfUserID = (String) parameter.get("selfUserID");
        String callID = (String) parameter.get("callID");
        List<String> target = (List<String>) parameter.get("callees");

        DatabaseCallUser databaseCallUser = new DatabaseCallUser();
        databaseCallUser.user_id = selfUserID;
        databaseCallUser.caller_id = selfUserID;
        long currentTimeMillis = System.currentTimeMillis();
        databaseCallUser.start_time = currentTimeMillis;
        databaseCallUser.status = Status.WAIT.getValue();
        Map<String, DatabaseCallUser> users = new HashMap<>();
        users.put(databaseCallUser.user_id, databaseCallUser);
        for (String userID : target) {
            DatabaseCallUser user = new DatabaseCallUser();
            user.user_id = userID;
            user.caller_id = selfUserID;
            user.start_time = currentTimeMillis;
            user.status = Status.WAIT.getValue();
            users.put(user.user_id, user);
        }
        DatabaseCall call = new DatabaseCall();
        call.call_id = callID;
        call.call_type = callType.getValue();
        call.users = users;
        call.call_status = Status.WAIT.getValue();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.setValue(call).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "startCallUser onSuccess() called with: unused = [" + unused + "]");
                addCallListener(callID);

                if (callback != null) {
                    callback.onResult(0, null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "startCallUser onFailure() called with: e = [" + e + "]");
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }

    void addCallListener(String callID) {
        String databaseRefPath = "call/" + callID;
        if (callmap.containsKey(databaseRefPath)) {
            return;
        }
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange() called with: snapshot = [" + snapshot + "]");
                if (snapshot.getValue() == null) { //  is already deleted
                    removeCallListener(callID);
                    return;
                }
                for (DataSnapshot child : snapshot.getChildren()) {
                    if ("users".equals(child.getKey())) {
                        HashMap<String, HashMap<String, Object>> value =
                            (HashMap<String, HashMap<String, Object>>) child.getValue();
                        Collection<HashMap<String, Object>> values = value.values();
                        for (HashMap<String, Object> hashMap : values) {
                            // not caller user node
                            if (!hashMap.containsKey("user_id") || !hashMap.containsKey("caller_id")) {
                                return;
                            }
                            String targetUserID = (String) hashMap.get("user_id");
                            String callerUserID = (String) hashMap.get("caller_id");
                            int status = ((Long) hashMap.get("status")).intValue();
                            if (!targetUserID.equals(callerUserID)) {
                                if (Status.WAIT.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("callee_id", targetUserID);
                                    data.put("call_id", callID);
                                    updater.receiveUpdate(ZegoListenerManager.RECEIVE_CALL, data);
                                } else if (Status.CONNECTED.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("callee_id", targetUserID);
                                    data.put("call_id", callID);
                                    updater.receiveUpdate(ZegoListenerManager.ACCEPT_CALL, data);
                                } else if (Status.FINISHED.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("id", targetUserID);
                                    data.put("call_id", callID);
                                    updater.receiveUpdate(ZegoListenerManager.END_CALL, data);
                                } else if (Status.DECLINED.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("callee_id", targetUserID);
                                    data.put("call_id", callID);
                                    data.put("type", "1");
                                    updater.receiveUpdate(ZegoListenerManager.DECLINE_CALL, data);
                                } else if (Status.BUSY.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("callee_id", targetUserID);
                                    data.put("call_id", callID);
                                    data.put("type", "2");
                                    updater.receiveUpdate(ZegoListenerManager.DECLINE_CALL, data);
                                } else if (Status.CANCELED.getValue() == status) {
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("caller_id", callerUserID);
                                    data.put("call_id", callID);
                                    updater.receiveUpdate(ZegoListenerManager.CANCEL_CALL, data);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        addDatabaseListener(databaseRefPath, listener);
    }


    private void removeCallListener(String callID) {
        Log.d(TAG, "removeCallListener() called with: callID = [" + callID + "]");
        removeDatabaseListener("call/" + callID);
    }

    private void acceptUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = (String) parameter.get("userID");
        String callID = (String) parameter.get("callID");

        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.CONNECTED.getValue());
        callUpdates.put("/users/" + selfUserID + "/status", Status.CONNECTED.getValue());
        callUpdates.put("/users/" + callerID + "/status", Status.CONNECTED.getValue());
        long currentTimeMillis = System.currentTimeMillis();
        callUpdates.put("/users/" + selfUserID + "/connected_time", currentTimeMillis);
        callUpdates.put("/users/" + callerID + "/connected_time", currentTimeMillis);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (callback != null) {
                    callback.onResult(0, null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }

    private void declineUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = (String) parameter.get("userID");
        String callID = (String) parameter.get("callID");

        int type = (int) parameter.get("type");
        int value;
        if (type == ZegoDeclineType.Decline.getValue()) {
            value = Status.DECLINED.getValue();
        } else {
            value = Status.BUSY.getValue();
        }
        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.FINISHED.getValue());
        callUpdates.put("/users/" + selfUserID + "/status", value);
        callUpdates.put("/users/" + callerID + "/status", value);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (callback != null) {
                    callback.onResult(0, null);
                }
                removeCallListener(callID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }

    private void cancelUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        String beCanceledUserID = (String) parameter.get("userID");
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = selfUserID;
        String callID = (String) parameter.get("callID");

        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.FINISHED.getValue());
        callUpdates.put("/users/" + beCanceledUserID + "/status", Status.CANCELED.getValue());
        callUpdates.put("/users/" + callerID + "/status", Status.CANCELED.getValue());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (callback != null) {
                    callback.onResult(0, null);
                }
                removeCallListener(callID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }

    private void endCallUser(Map<String, Object> parameter, ZegoRequestCallback callback) {
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = selfUserID;
        String callID = (String) parameter.get("callID");

        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.FINISHED.getValue());
        //todo make all user Status.FINISHED
        callUpdates.put("/users/" + callerID + "/status", Status.FINISHED.getValue());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (callback != null) {
                    callback.onResult(0, null);
                }
                removeCallListener(callID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }


    private void queryDatabaseForOnlineUser(ZegoRequestCallback callback) {
        callback.onResult(0, onlineUserList);
    }

    private void signOutFirebaseAuth(Map<String, Object> parameter, ZegoRequestCallback callback) {
        FirebaseAuth.getInstance().signOut();
        String userID = (String) parameter.get("selfUserID");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference onlineUserRef = database.getReference("online_user").child(userID);
        onlineUserRef.removeValue();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration databasePushToken failed",
                        task.getException());
                    return;
                }
                String pushToken = task.getResult();
                Log.d(TAG, "removeValue() pushTokenRef with: task = [" + "]");
                DatabaseReference pushTokenRef = database.getReference("push_token")
                    .child(userID).child(pushToken);
                pushTokenRef.removeValue();
            }
        });
        if (callback != null) {
            callback.onResult(0, null);
        }
    }

    private void firebaseAuthWithGoogle(String authToken, ZegoRequestCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(authToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        onFirebaseAuthSuccess(callback);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (callback != null) {
                            callback.onResult(-1000, null);
                        }
                    }
                }
            });
    }

    private void onFirebaseAuthSuccess(ZegoRequestCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration databasePushToken failed",
                        task.getException());
                    FirebaseAuth.getInstance().signOut();
                    if (callback != null) {
                        callback.onResult(-1000, null);
                    }
                    return;
                }
                // Get new FCM registration databasePushToken
                String pushToken = task.getResult();
                Log.d(TAG, "onComplete() called with: databasePushToken = [" + pushToken + "]");

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference onlineUserRef = database.getReference("online_user")
                    .child(currentUser.getUid());
                DatabaseUser databaseUser = new DatabaseUser();
                databaseUser.display_name = currentUser.getDisplayName();
                databaseUser.user_id = currentUser.getUid();
                databaseUser.last_changed = ServerValue.TIMESTAMP;
                databaseUser.token_id = pushToken;
                onlineUserRef.setValue(databaseUser);

                DatabaseReference pushTokenRef = database.getReference("push_token")
                    .child(currentUser.getUid()).child(pushToken);
                Map<String, Object> updates = new HashMap<>();
                updates.put("device_type", "android");
                updates.put("user_id", currentUser.getUid());
                updates.put("token_id", pushToken);
                pushTokenRef.setValue(updates);

                Map<String, Object> user = new HashMap<>();
                user.put("userID", currentUser.getUid());
                user.put("userName", currentUser.getDisplayName());
                if (callback != null) {
                    callback.onResult(0, user);
                }
            }
        });

    }

    private void listenUserOnline() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference onlineUserRef = database.getReference("online_user");

        ValueEventListener onlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "online_user changed: getChildrenCount = [" + snapshot.getChildrenCount() + "]");
                onlineUserList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    HashMap<String, Object> value = (HashMap<String, Object>) child.getValue();
                    ZegoUserInfo userInfo = new ZegoUserInfo();
                    userInfo.userName = (String) value.get("display_name");
                    userInfo.userID = (String) value.get("user_id");
                    onlineUserList.add(userInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        onlineUserRef.orderByChild("last_changed").addValueEventListener(onlineListener);
        callmap.put("online_user", onlineListener);
    }

    private void initConnectListener() {
        Log.d(TAG, "initOnlineListener() called");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        ValueEventListener connectListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (connected) {
                    if (currentUser != null) {
                        DatabaseReference onlineUserRef = database.getReference("online_user")
                            .child(currentUser.getUid());
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(
                            new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        return;
                                    }
                                    String pushToken = task.getResult();
                                    DatabaseUser databaseUser = new DatabaseUser();
                                    databaseUser.display_name = currentUser.getDisplayName();
                                    databaseUser.user_id = currentUser.getUid();
                                    databaseUser.last_changed = ServerValue.TIMESTAMP;
                                    databaseUser.token_id = pushToken;
                                    onlineUserRef.setValue(databaseUser);
                                    onlineUserRef.onDisconnect().removeValue();
                                }
                            });
                    }
                } else {
                    if (currentUser != null) {
                        DatabaseReference onlineUserRef = database.getReference("online_user")
                            .child(currentUser.getUid());
                        onlineUserRef.removeValue();
                    }
                }
                Log.d(TAG, "info/connected., called with: connected = [" + connected + "]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        addDatabaseListener(".info/connected", connectListener);
    }

    private void removeDatabaseListener(String path) {
        if (callmap.containsKey(path)) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            ValueEventListener valueEventListener = callmap.remove(path);
            if (valueEventListener != null) {
                database.getReference(path).removeEventListener(valueEventListener);
            }
        }
    }

    private void addDatabaseListener(String path, ValueEventListener listener) {
        if (!callmap.containsKey(path)) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference(path).addValueEventListener(listener);
            callmap.put(path, listener);
        }
    }

    public void unInitListeners() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (Entry<String, ValueEventListener> entry : callmap.entrySet()) {
            database.getReference(entry.getKey()).removeEventListener(entry.getValue());
        }
    }
}
