/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package im.zego.call;

import android.text.TextUtils;
import android.util.Log;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.model.DatabaseCall;
import im.zego.callsdk.model.DatabaseCall.DatabaseCallUser;
import im.zego.callsdk.model.ZegoCallInfo;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            handleNow(data);
        }

        // Check if message contains a notification payload.
        Notification messageNotification = remoteMessage.getNotification();
        if (messageNotification != null) {
            Log.d(TAG, "Message Notification Body: " + messageNotification.getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called: 1) When a new token is generated on initial app startup 2)
     * Whenever an existing token is changed Under #2, there are three scenarios when the existing token is changed: A)
     * App is restored to a new device B) User uninstalls/reinstalls the app C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        //        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
        //                .build();
        //        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     *
     * @param data
     */
    private void handleNow(Map<String, String> data) {
        Log.d(TAG, "handleNow() called with: data = [" + data + "]");
        ZegoUserInfo caller = new ZegoUserInfo();
        caller.userID = data.get("caller_id");
        caller.userName = data.get("caller_name");
        String callID = data.get("call_id");
        String callType = data.get("call_type");
        String callData = data.get("call_data");
        DatabaseCall databaseCall = ZegoServiceManager.getInstance().mGson.fromJson(callData, DatabaseCall.class);

        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        if (!TextUtils.isEmpty(callService.getCallInfo().callID)) {
            callService.declineCall(caller.userID, ZegoDeclineType.Busy, new ZegoCallback() {
                @Override
                public void onResult(int errorCode) {
                    Log.d(TAG, "declineCall Busy,called with: errorCode = [" + errorCode + "]");
                }
            });
            return;
        }
        ZegoCallType type = ZegoCallType.Voice;
        for (ZegoCallType zegoCallType : ZegoCallType.values()) {
            if (zegoCallType.getValue() == Integer.parseInt(callType)) {
                type = zegoCallType;
                break;
            }
        }
        ZegoCallInfo callInfo = new ZegoCallInfo();
        callInfo.caller = caller;
        callInfo.callID = callID;
        callInfo.users = new ArrayList<>();
        for (DatabaseCallUser databaseCallUser : databaseCall.users.values()) {
            ZegoUserInfo userInfo = new ZegoUserInfo();
            userInfo.userID = databaseCallUser.user_id;
            userInfo.userName = databaseCallUser.user_name;
            callInfo.users.add(userInfo);
        }
        callService.setCallInfo(callInfo);
        ZegoCallServiceListener listener = callService.getListener();
        if (listener != null) {
            ZegoCallType finalType = type;
            ThreadUtils.runOnUiThread(() -> {
                listener.onReceiveCallInvite(caller, finalType);
            });
        }
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM registration token with any server-side account maintained by your
     * application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}
