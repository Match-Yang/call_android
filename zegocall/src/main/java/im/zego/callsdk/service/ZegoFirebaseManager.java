package im.zego.callsdk.service;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
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
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoFirebaseManager implements ZegoRequestProtocol {

    private ZegoListenerUpdater updater;
    private static final String TAG = "ZegoFirebaseManager";
    private DatabaseReference connectedReference;
    private List<ZegoUserInfo> onlineUserList = new ArrayList();

    public ZegoFirebaseManager() {
        initConnectListener();
        listenUserOnline();
    }

    @Override
    public void request(String path, Map<String, Object> parameter, ZegoRequestCallback callback) {
        if (ZegoCommand.LOGIN.equals(path)) {
            String token = (String) parameter.get("token");
            firebaseAuthWithGoogle(token, callback);
        } else if (ZegoCommand.LOGOUT.equals(path)) {
            signOutFirebaseAuth(callback);
        } else if (ZegoCommand.GET_USER_LIST.equals(path)) {
            queryDatabaseForOnlineUser(callback);
        } else if (ZegoCommand.START_CALL.equals(path)) {

        } else if (ZegoCommand.END_CALL.equals(path)) {

        } else if (ZegoCommand.RESPOND_CALL.equals(path)) {

        } else if (ZegoCommand.CANCEL_CALL.equals(path)) {

        }
    }

    private void queryDatabaseForOnlineUser(ZegoRequestCallback callback) {
        callback.onResult(0, onlineUserList);
    }

    private void signOutFirebaseAuth(ZegoRequestCallback callback) {
        FirebaseAuth.getInstance().signOut();
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        String userID = userService.localUserInfo.userID;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference onlineUserRef = database.getReference("online_user").child(userID);
        onlineUserRef.removeValue();
        DatabaseReference pushTokenRef = database.getReference("push_token").child(userID);
        pushTokenRef.removeValue();
        if (callback != null) {
            callback.onResult(0, null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken, ZegoRequestCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                        userService.localUserInfo = new ZegoUserInfo();
                        userService.localUserInfo.userID = currentUser.getUid();
                        userService.localUserInfo.userName = currentUser.getDisplayName();
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration databasePushToken failed", task.getException());
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
                                    .child(pushToken);
                                DatabasePushToken databasePushToken = new DatabasePushToken();
                                databasePushToken.user_id = currentUser.getUid();
                                databasePushToken.token_id = pushToken;
                                databasePushToken.device_type = "android";
                                pushTokenRef.setValue(databasePushToken);


                                if (callback != null) {
                                    callback.onResult(0, null);
                                }
                            }
                        });
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

    private void listenUserOnline() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference onlineUserRef = database.getReference("online_user");
        onlineUserRef.orderByChild("last_changed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "online_user changed: getChildrenCount = [" + snapshot.getChildrenCount() + "]");
                onlineUserList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Log.d(TAG,
                        "online_user changed, child.getKey():" + child.getKey() + ",getValue:" + child.getValue());
                    HashMap<String, Object> value = (HashMap<String, Object>) child.getValue();
                    ZegoUserInfo userInfo = new ZegoUserInfo();
                    userInfo.userName = (String) value.get("display_name");
                    userInfo.userID = (String) value.get("uid");
                    onlineUserList.add(userInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initConnectListener() {
        Log.d(TAG, "initOnlineListener() called");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        DatabaseReference onlineUserRef = database.getReference("online_user");
        onlineUserRef.keepSynced(true);
        connectedReference = database.getReference(".info/connected");
        connectedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (connected) {
                    if (currentUser != null) {
                        DatabaseReference onlineUserRef = database.getReference("online_user")
                            .child(currentUser.getUid());
                        DatabaseUser databaseUser = new DatabaseUser();
                        databaseUser.display_name = currentUser.getDisplayName();
                        databaseUser.user_id = currentUser.getUid();
                        databaseUser.last_changed = ServerValue.TIMESTAMP;
                        onlineUserRef.setValue(databaseUser);
                        onlineUserRef.onDisconnect().removeValue();
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
        });
    }
}
