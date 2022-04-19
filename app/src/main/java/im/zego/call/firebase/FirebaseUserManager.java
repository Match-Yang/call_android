package im.zego.call.firebase;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import im.zego.call.token.ZegoTokenManager;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.model.DatabaseUser;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirebaseUserManager {

    private static final String TAG = "FirebaseUserManager";

    private static volatile FirebaseUserManager singleton = null;

    private ValueEventListener connectListener;
    private ValueEventListener onlineListener;
    private ValueEventListener selfOnlineListener;
    private boolean isSelfOnlineListenerAdded;

    OnUserSignInOtherDeviceListener signInOtherDeviceListener;

    private FirebaseUserManager() {
        FirebaseAuth.getInstance().addAuthStateListener(new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG,
                    "onAuthStateChanged() called with: firebaseAuth = [" + (firebaseAuth.getUid())
                        + "]");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                if (connectListener == null) {
                    connectListener = getDatabaseConnectListener();
                }
                if (onlineListener == null) {
                    onlineListener = getOnlineUserListener();
                }
                if (firebaseAuth.getCurrentUser() != null) {
                    database.getReference(".info/connected")
                        .addValueEventListener(connectListener);
                    database.getReference("online_user").orderByChild("last_changed")
                        .addValueEventListener(onlineListener);
                } else {
                    database.getReference("online_user").removeEventListener(onlineListener);
                    database.getReference(".info/connected").removeEventListener(connectListener);
                    ZegoTokenManager.getInstance().reset();
                }
            }
        });
    }

    public static FirebaseUserManager getInstance() {
        if (singleton == null) {
            synchronized (FirebaseUserManager.class) {
                if (singleton == null) {
                    singleton = new FirebaseUserManager();
                }
            }
        }
        return singleton;
    }

    private List<ZegoUserInfo> onlineUserList = new ArrayList();

    public void signInFirebase(String authToken, ZegoCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(authToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG,  "signInWithCredential:success");
                        onFirebaseAuthSuccess(callback);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (callback != null) {
                            callback.onResult(-1000);
                        }
                    }
                }
            });
    }

    public FirebaseUser getCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            addSelfOnlineListener(currentUser.getUid());
        }
        return currentUser;

    }

    private void onFirebaseAuthSuccess(ZegoCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration databasePushToken failed",
                        task.getException());
                    FirebaseAuth.getInstance().signOut();
                    if (callback != null) {
                        callback.onResult(-1000);
                    }
                    return;
                }
                // Get new FCM registration databasePushToken
                String pushToken = task.getResult();
                Log.d(TAG,  "onComplete() called with: databasePushToken = [" + pushToken + "]");

                makeSelfOnline(pushToken, currentUser);

                addDevicePushTokenToUser(pushToken, currentUser);

                addSelfOnlineListener(currentUser.getUid());

                if (callback != null) {
                    callback.onResult(0);
                }


            }
        });

    }

    private void addDevicePushTokenToUser(String pushToken, FirebaseUser currentUser) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userPushRef = database.getReference("push_token")
            .child(currentUser.getUid());
        userPushRef.removeValue();
        DatabaseReference tokenRef = userPushRef.child(pushToken);
        Map<String, Object> updates = new HashMap<>();
        updates.put("device_type", "android");
        updates.put("user_id", currentUser.getUid());
        updates.put("token_id", pushToken);
        tokenRef.setValue(updates);
    }

    @NonNull
    private void makeSelfOnline(String pushToken, FirebaseUser currentUser) {
        Log.d(TAG,
            "makeSelfOnline() called with: pushToken = [" + pushToken + "], currentUser = [" + currentUser + "]");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference onlineUserRef = database.getReference("online_user")
            .child(currentUser.getUid());
        DatabaseUser databaseUser = new DatabaseUser();
        databaseUser.display_name = currentUser.getDisplayName();
        databaseUser.user_id = currentUser.getUid();
        databaseUser.last_changed = ServerValue.TIMESTAMP;
        databaseUser.token_id = pushToken;
        onlineUserRef.setValue(databaseUser);
    }

    public void signOutFirebaseAuth() {
        Log.d(TAG,  "signOutFirebaseAuth() called");
        String userID = FirebaseAuth.getInstance().getUid();
        if (userID == null) {
            return;
        }
        FirebaseAuth.getInstance().signOut();

        makeSelfOffline(userID);

        removePushToken(userID);

        removeSelfOnlineListener(userID);
    }

    private void addSelfOnlineListener(String uid) {
        Log.d(TAG,  "addSelfOnlineListener() called with: uid = [" + uid
            + "],isSelfOnlineListenerAdded:" + isSelfOnlineListenerAdded);
        if (selfOnlineListener == null) {
            selfOnlineListener = getSelfOnlineListener();
        }
        if (isSelfOnlineListenerAdded) {
            return;
        }
        isSelfOnlineListenerAdded = true;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("online_user").child(uid)
            .addValueEventListener(selfOnlineListener);
    }

    private void removeSelfOnlineListener(String uid) {
        Log.d(TAG,  "removeSelfOnlineListener() called with: uid = [" + uid
            + "],isSelfOnlineListenerAdded" + isSelfOnlineListenerAdded);
        if (!isSelfOnlineListenerAdded) {
            return;
        }
        if (selfOnlineListener != null) {
            isSelfOnlineListenerAdded = false;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("online_user").child(uid)
                .removeEventListener(selfOnlineListener);
        }
    }

    private void makeSelfOffline(String userID) {
        Log.d(TAG,  "removeSelfOnline() called with: userID = [" + userID + "]");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference selfRef = database.getReference("online_user").child(userID);
        selfRef.removeValue();
    }

    private void removePushToken(String userID) {
        Log.d(TAG,  "removePushToken() called with: userID = [" + userID + "]");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration databasePushToken failed",
                        task.getException());
                    return;
                }
                String pushToken = task.getResult();
                DatabaseReference pushTokenRef = database.getReference("push_token")
                    .child(userID).child(pushToken);
                pushTokenRef.removeValue();
            }
        });
    }

    private ValueEventListener getOnlineUserListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onlineUserList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    HashMap<String, Object> value = (HashMap<String, Object>) child.getValue();
                    String user_id = (String) value.get("user_id");
                    String selfUID = FirebaseAuth.getInstance().getUid();
                    if (!Objects.equals(selfUID, user_id)) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userName = (String) value.get("display_name");
                        userInfo.userID = user_id;
                        onlineUserList.add(userInfo);
                    }
                }
                Log.d(TAG,  "onlineUserList() changed = [" + onlineUserList.size() + "]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private ValueEventListener getDatabaseConnectListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                boolean connected = snapshot.getValue(Boolean.class);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (connected) {
                    if (currentUser != null) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            String pushToken = task.getResult();
                            makeSelfOnline(pushToken, currentUser);
                            DatabaseReference onlineUserRef = database.getReference("online_user")
                                .child(currentUser.getUid());
                            onlineUserRef.onDisconnect().removeValue();
                        });
                    }
                } else {
                    if (currentUser != null) {
                        DatabaseReference onlineUserRef = database.getReference("online_user")
                            .child(currentUser.getUid());
                        onlineUserRef.removeValue();
                    }
                }
                Log.d(TAG,  "info/connected., called with: connected = [" + connected + "]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private ValueEventListener getSelfOnlineListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG,  "getSelfOnlineListener() called with: snapshot = [" + snapshot + "]");
                if (snapshot.getValue() != null) {
                    DatabaseUser databaseUser = snapshot.getValue(DatabaseUser.class);
                    if (databaseUser == null) {
                        return;
                    }
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String pushToken = task.getResult();
                        Log.d(TAG,  "getSelfOnlineListener pushToken: " + pushToken);
                        Log.d(TAG,  "getSelfOnlineListener token_id:  " + databaseUser.token_id);
                        if (!Objects.equals(pushToken, databaseUser.token_id)) {
                            String userID = FirebaseAuth.getInstance().getUid();
                            if (userID == null) {
                                return;
                            }
                            FirebaseAuth.getInstance().signOut();
                            Log.d(TAG,  "sign out because of other device sign in same account");
                            removeSelfOnlineListener(userID);
                            removePushToken(userID);
                            if (signInOtherDeviceListener != null) {
                                signInOtherDeviceListener.onSignIn();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public List<ZegoUserInfo> getOnlineUserList() {
        return onlineUserList;
    }

    public void setSignInOtherDeviceListener(OnUserSignInOtherDeviceListener listener) {
        this.signInOtherDeviceListener = listener;
    }

    public interface OnUserSignInOtherDeviceListener {

        void onSignIn();
    }
}
