package im.zego.callsdk.request;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoRequestProtocol;
import im.zego.callsdk.core.commands.ZegoCommand;
import im.zego.callsdk.listener.ZegoListenerManager;
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
import java.util.Objects;

public class ZegoFirebaseManager implements ZegoRequestProtocol {

    private ZegoListenerUpdater updater;
    private static final String TAG = "ZegoFirebaseManager";
    private List<ZegoUserInfo> onlineUserList = new ArrayList();
    private Map<String, ValueEventListener> callmap = new HashMap<>();
    private ChildEventListener childEventListener;
    private Map<String, DatabaseCall> selfCalls = new HashMap<>();

    public ZegoFirebaseManager() {
        Log.d(TAG, "ZegoFirebaseManager() called");
        updater = ZegoListenerManager.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        FirebaseAuth.getInstance().addAuthStateListener(new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG,
                    "onAuthStateChanged() called with: firebaseAuth = [" + (firebaseAuth.getCurrentUser() != null)
                        + "]");
                if (firebaseAuth.getCurrentUser() != null) {
                    listenerDatabaseConnect();
                    listenUserOnline();
                    listenUserCall();
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    if (childEventListener != null) {
                        database.getReference("/call").removeEventListener(childEventListener);
                    }
                    removeDatabaseListener("online_user");
                    removeDatabaseListener(".info/connected");
                }
            }
        });
    }

    private void unInitOnlineListener() {

    }

    @Override
    public void request(String path, Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG,
            "request() called with: path = [" + path + "], parameter = [" + parameter + "], callback = [" + callback
                + "]");
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
            //            String callID = (String) parameter.get("callID");
            //            addCallListener(callID);
        } else if (ZegoCommand.GET_TOKEN.equals(path)) {
            getTokenFromCloudFunction(parameter, callback);
        } else if (ZegoCommand.HEARTBEAT.equals(path)) {
            sendHeartBeat(parameter, callback);
        }
    }

    private void getTokenFromCloudFunction(Map<String, Object> parameter,
        ZegoRequestCallback callback) {
        Log.d(TAG,
            "getTokenFromCloudFunction() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        String userID = (String) parameter.get("userID");
        Long time = (Long) parameter.get("effectiveTime");
        Map<String, Object> data = new HashMap<>();
        data.put("id", userID);
        data.put("effective_time", time);

        FirebaseFunctions.getInstance().getHttpsCallable("getToken")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, Object>() {
                @Override
                public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    return task.getResult().getData();
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                        }
                        if (callback != null) {
                            callback.onResult(-1000, e);
                        }
                        return;
                    }
                    HashMap<String, String> result = (HashMap<String, String>) task.getResult();
                    if (callback != null) {
                        callback.onResult(0, result.get("token"));
                    }
                }
            });
    }

    private void sendHeartBeat(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "sendHeartBeat() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        String callID = (String) parameter.get("callID");
        String userID = (String) parameter.get("userID");
        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/users/" + userID + "/heartbeat_time", ServerValue.TIMESTAMP);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void startCallUser(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "startCallUser() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        ZegoCallType callType = (ZegoCallType) parameter.get("callType");
        HashMap<String, String> caller = (HashMap<String, String>) parameter.get("caller");
        String callID = (String) parameter.get("callID");

        Map<String, DatabaseCallUser> users = new HashMap<>();
        DatabaseCallUser callUser = new DatabaseCallUser();
        callUser.user_id = caller.get("id");
        callUser.user_name = caller.get("name");
        callUser.caller_id = caller.get("id");
        long currentTimeMillis = System.currentTimeMillis();
        callUser.start_time = currentTimeMillis;
        callUser.status = Status.WAIT.getValue();
        users.put(callUser.user_id, callUser);

        List<HashMap<String, String>> list = (List<HashMap<String, String>>) parameter.get("callees");
        for (HashMap<String, String> hashMap : list) {
            String id = hashMap.get("id");
            String name = hashMap.get("name");
            DatabaseCallUser targetUser = new DatabaseCallUser();
            targetUser.user_id = id;
            targetUser.user_name = name;
            targetUser.caller_id = caller.get("id");
            targetUser.start_time = currentTimeMillis;
            targetUser.status = Status.WAIT.getValue();
            users.put(targetUser.user_id, targetUser);
        }

        DatabaseCall databaseCall = new DatabaseCall();
        databaseCall.call_id = callID;
        databaseCall.call_type = callType.getValue();
        databaseCall.users = users;
        databaseCall.call_status = Status.WAIT.getValue();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.setValue(databaseCall).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "startCallUser onSuccess() called with: unused = [" + unused + "]");
                addCallListener(databaseCall);

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
                selfCalls.remove(callID);
            }
        });
        selfCalls.put(callID, databaseCall);
    }

    private boolean isCallIDContainsSelf(DataSnapshot snapshot) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseCall databaseCall = snapshot.getValue(DatabaseCall.class);
        boolean result = false;
        for (DatabaseCallUser value : databaseCall.users.values()) {
            if (Objects.equals(currentUser.getUid(), value.user_id)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private DatabaseCall currentCallData;

    private void listenUserCall() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                Log.d(TAG, "onChildAdded() called with: snapshot = [" + snapshot + "], previousChildName = ["
                    + previousChildName + "]");
                DatabaseCall databaseCall = snapshot.getValue(DatabaseCall.class);
                if (!isCallIDContainsSelf(snapshot) || databaseCall.call_status == 0) {
                    return;
                }
                DatabaseCallUser caller = new DatabaseCallUser();
                DatabaseCallUser receiver = new DatabaseCallUser();
                for (DatabaseCallUser user : databaseCall.users.values()) {
                    if (!TextUtils.isEmpty(user.user_id)) {
                        if (Objects.equals(user.user_id, user.caller_id)) {
                            caller = user;
                        } else {
                            receiver = user;
                        }
                    }
                }
                boolean isSelfCaller = Objects.equals(currentUser.getUid(), caller.user_id);
                if (isSelfCaller) {
                    return;
                }
                if (selfCalls.isEmpty()) {
                    selfCalls.put(databaseCall.call_id, databaseCall);
                    HashMap<String, Object> data = new HashMap<>();
                    HashMap<String, String> callerData = new HashMap<>();
                    callerData.put("id", caller.user_id);
                    callerData.put("name", caller.user_name);
                    data.put("caller", callerData);
                    List<HashMap<String, String>> calleeData = new ArrayList<>();
                    HashMap<String, String> callee = new HashMap<>();
                    callee.put("id", receiver.user_id);
                    callee.put("name", receiver.user_name);
                    calleeData.add(callee);
                    data.put("callees", calleeData);
                    data.put("call_id", databaseCall.call_id);
                    data.put("type", databaseCall.call_type);
                    addCallListener(databaseCall);
                    updater.receiveUpdate(ZegoListenerManager.RECEIVE_CALL, data);
                } else {
                    declineCallInner(currentUser.getUid(), caller.caller_id,
                        databaseCall.call_id, databaseCall.call_type, null);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildChanged() called with: snapshot = [" + snapshot + "], previousChildName = ["
                    + previousChildName + "]");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onChildRemoved() called with: snapshot = [" + snapshot + "]");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        database.getReference("/call").addChildEventListener(childEventListener);
    }

    private void addCallListener(DatabaseCall databaseCall) {
        String callID = databaseCall.call_id;
        Log.d(TAG, "addCallListener() called with: callID = [" + callID + "]");
        String databaseRefPath = "call/" + callID;
        if (callmap.containsKey(databaseRefPath)) {
            return;
        }
        currentCallData = null;
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange() called with: snapshot = [" + snapshot + "]");
                if (snapshot.getValue() == null) { //  is already deleted
                    processCallIDRemoved(callID);
                    return;
                }
                DatabaseCall previousCall = currentCallData;
                DatabaseCall changedValue = snapshot.getValue(DatabaseCall.class);
                if (changedValue == null || changedValue.call_status == 0) {
                    // 0 means set heartbeat to removed call
                    return;
                }
                currentCallData = changedValue;

                DatabaseCallUser caller = new DatabaseCallUser();
                DatabaseCallUser receiver = new DatabaseCallUser();
                for (DatabaseCallUser user : changedValue.users.values()) {
                    if (TextUtils.isEmpty(user.user_id)) {
                        return;
                    }
                    String targetUserID = user.user_id;
                    String callerUserID = user.caller_id;
                    if (Objects.equals(callerUserID, targetUserID)) {
                        caller = user;
                    } else {
                        receiver = user;
                    }
                }
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                boolean isSelfCaller = Objects.equals(currentUser.getUid(), caller.user_id);
                DatabaseCallUser self = isSelfCaller ? caller : receiver;
                DatabaseCallUser other = isSelfCaller ? receiver : caller;

                if (previousCall != null) {
                    boolean callStatusChanged = (previousCall.call_status != changedValue.call_status);
                    boolean callerStatusChanged = (previousCall.users.get(caller.user_id).status
                        != changedValue.users.get(caller.user_id).status);
                    boolean receiverStatusChanged = (previousCall.users.get(receiver.user_id).status
                        != changedValue.users.get(receiver.user_id).status);
                    if (!callStatusChanged && !callerStatusChanged && !receiverStatusChanged) {
                        // no status changed , is heartbeat update
                        if (caller.heartbeat_time != 0 && receiver.heartbeat_time != 0) {
                            if (Math.abs(caller.heartbeat_time - receiver.heartbeat_time) > 30_000) {
                                HashMap<String, String> data = new HashMap<>();
                                // i receive,means the other one timeout
                                data.put("user_id", other.user_id);
                                data.put("call_id", callID);
                                updater.receiveUpdate(ZegoListenerManager.TIMEOUT_CALL, data);
                            }
                        }
                        return;
                    }
                }
                if (changedValue.call_status == Status.FINISHED.getValue()) {
                    if (caller.status == Status.FINISHED.getValue()
                        || receiver.status == Status.FINISHED.getValue()) {
                        HashMap<String, String> data = new HashMap<>();
                        // i receive,means the other one is the operator,because
                        // the operator has already removed call listener
                        data.put("id", other.user_id);
                        data.put("call_id", callID);
                        updater.receiveUpdate(ZegoListenerManager.END_CALL, data);
                    } else if (Status.DECLINED.getValue() == caller.status) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("callee_id", receiver.user_id);
                        data.put("call_id", callID);
                        data.put("type", "1");
                        updater.receiveUpdate(ZegoListenerManager.DECLINE_CALL, data);
                    } else if (Status.BUSY.getValue() == caller.status) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("callee_id", receiver.user_id);
                        data.put("call_id", callID);
                        data.put("type", "2");
                        updater.receiveUpdate(ZegoListenerManager.DECLINE_CALL, data);
                    } else if (Status.CANCELED.getValue() == caller.status) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("caller_id", caller.user_id);
                        data.put("callee_id", receiver.user_id);
                        data.put("call_id", callID);
                        updater.receiveUpdate(ZegoListenerManager.CANCEL_CALL, data);
                    }
                } else {
                    if (receiver.status == Status.CONNECTED.getValue()) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("callee_id", receiver.user_id);
                        data.put("call_id", callID);
                        updater.receiveUpdate(ZegoListenerManager.ACCEPT_CALL, data);
                    } else if (receiver.status == Status.WAIT.getValue()) {
                        if (selfCalls.containsKey(changedValue.call_id)) {
                            return;
                        }
                        HashMap<String, Object> data = new HashMap<>();
                        HashMap<String, String> callerData = new HashMap<>();
                        callerData.put("id", caller.user_id);
                        callerData.put("name", caller.user_name);
                        data.put("caller", callerData);
                        List<HashMap<String, String>> calleeData = new ArrayList<>();
                        HashMap<String, String> callee = new HashMap<>();
                        callee.put("id", receiver.user_id);
                        callee.put("name", receiver.user_name);
                        calleeData.add(callee);
                        data.put("callees", calleeData);
                        data.put("call_id", callID);
                        data.put("type", changedValue.call_type);
                        updater.receiveUpdate(ZegoListenerManager.RECEIVE_CALL, data);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        addDatabaseListener(databaseRefPath, listener);
    }

    private void processCallIDRemoved(String callID) {
        if (currentCallData != null) {
            DatabaseCallUser caller = new DatabaseCallUser();
            DatabaseCallUser receiver = new DatabaseCallUser();
            for (DatabaseCallUser user : currentCallData.users.values()) {
                if (TextUtils.isEmpty(user.user_id)) {
                    return;
                }
                String targetUserID = user.user_id;
                String callerUserID = user.caller_id;
                if (Objects.equals(callerUserID, targetUserID)) {
                    caller = user;
                } else {
                    receiver = user;
                }
            }
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            boolean isSelfCaller = Objects.equals(currentUser.getUid(), caller.user_id);
            DatabaseCallUser self = isSelfCaller ? caller : receiver;
            DatabaseCallUser other = isSelfCaller ? receiver : caller;
            HashMap<String, String> data = new HashMap<>();
            if (currentCallData.call_status == 1) {
                if (isSelfCaller) {
                    data.put("callee_id", receiver.user_id);
                    data.put("call_id", callID);
                    data.put("type", "1");
                    updater.receiveUpdate(ZegoListenerManager.DECLINE_CALL, data);
                } else {
                    data.put("caller_id", caller.user_id);
                    data.put("callee_id", receiver.user_id);
                    data.put("call_id", callID);
                    updater.receiveUpdate(ZegoListenerManager.CANCEL_CALL, data);
                }
            } else {
                data.put("id", other.user_id);
                data.put("call_id", callID);
                updater.receiveUpdate(ZegoListenerManager.END_CALL, data);
            }
        }
        currentCallData = null;
        removeCallListener(callID);
    }


    private void removeCallListener(String callID) {
        removeDatabaseListener("call/" + callID);
        selfCalls.remove(callID);
    }

    private void acceptUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "acceptUserCall() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = (String) parameter.get("userID");
        String callID = (String) parameter.get("callID");

        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.CONNECTED.getValue());
        callUpdates.put("/users/" + selfUserID + "/status", Status.CONNECTED.getValue());
        callUpdates.put("/users/" + callerID + "/status", Status.CONNECTED.getValue());
        callUpdates.put("/users/" + selfUserID + "/connected_time", ServerValue.TIMESTAMP);
        callUpdates.put("/users/" + callerID + "/connected_time", ServerValue.TIMESTAMP);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "acceptUserCall onSuccess() called with: unused = [" + unused + "]");
                if (callback != null) {
                    callback.onResult(0, callID);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "acceptUserCall onFailure() called with: e = [" + e + "]");
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
    }

    private void declineUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "declineUserCall() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = (String) parameter.get("userID");
        String callID = (String) parameter.get("callID");
        int type = (int) parameter.get("type");

        declineCallInner(selfUserID, callerID, callID, type, callback);
    }

    private void declineCallInner(String selfUserID, String callerID, String callID, int type,
        ZegoRequestCallback callback) {
        Log.d(TAG, "declineCallInner() called with: selfUserID = [" + selfUserID + "], callerID = [" + callerID
            + "], callID = [" + callID + "], type = [" + type + "], callback = [" + callback + "]");
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
        callRef.updateChildren(callUpdates)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "declineCallInner onSuccess() called with: unused = [" + unused + "]");
                    if (callback != null) {
                        callback.onResult(0, callID);
                    }

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "declineCallInner onFailure() called with: e = [" + e + "]");
                    if (callback != null) {
                        callback.onResult(-1000, null);
                    }
                }
            });
        removeCallListener(callID);
    }

    private void cancelUserCall(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "cancelUserCall() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
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
                Log.d(TAG, "cancelUserCall onSuccess() called with: unused = [" + unused + "]");
                if (callback != null) {
                    callback.onResult(0, callID);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "cancelUserCall onFailure() called with: e = [" + e + "]");
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
        removeCallListener(callID);
    }

    private void endCallUser(Map<String, Object> parameter, ZegoRequestCallback callback) {
        Log.d(TAG, "endCallUser() called with: parameter = [" + parameter + "], callback = [" + callback + "]");
        String selfUserID = (String) parameter.get("selfUserID");
        String callerID = selfUserID;
        String callID = (String) parameter.get("callID");

        Map<String, Object> callUpdates = new HashMap<>();
        callUpdates.put("/call_status", Status.FINISHED.getValue());
        if (currentCallData != null) {
            if (Objects.equals(currentCallData.call_id, callID)) {
                Collection<DatabaseCallUser> values = currentCallData.users.values();
                for (DatabaseCallUser value : values) {
                    callUpdates.put("/users/" + value.user_id + "/status", Status.FINISHED.getValue());
                    callUpdates.put("/users/" + value.user_id + "/finish_time", ServerValue.TIMESTAMP);
                }
            } else {
                callUpdates.put("/users/" + selfUserID + "/status", Status.FINISHED.getValue());
                callUpdates.put("/users/" + selfUserID + "/finish_time", ServerValue.TIMESTAMP);
            }
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference callRef = database.getReference("call").child(callID);
        callRef.updateChildren(callUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "endCallUser onSuccess() called with: unused = [" + unused + "]");
                if (callback != null) {
                    callback.onResult(0, callID);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "endCallUser onFailure() called with: e = [" + e + "]");
                if (callback != null) {
                    callback.onResult(-1000, null);
                }
            }
        });
        removeCallListener(callID);
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

    private void listenerDatabaseConnect() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
