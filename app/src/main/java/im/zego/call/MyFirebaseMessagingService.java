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

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
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

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();

        boolean isAppNotStart = !AppUtils.isAppForeground() && ActivityUtils.getActivityList().isEmpty();
        boolean isDeviceRestart = AppUtils.isAppForeground() && ActivityUtils.getActivityList().isEmpty();
        if (isAppNotStart || isDeviceRestart) {
            if (data.size() > 0) {
                AppUtils.relaunchApp();
            }
        }
        //        if (isAppNotStart) {
        //            if (data.size() > 0) {
        //                AppUtils.relaunchApp();
        //                new Handler(Looper.getMainLooper()).postDelayed(() -> {
        //                    handleNow(data);
        //                }, 200);
        //            }
        //        } else if (isDeviceRestart) {
        //            if (data.size() > 0) {
        //                AppUtils.relaunchApp();
        //                new Handler(Looper.getMainLooper()).postDelayed(() -> {
        //                    handleNow(data);
        //                }, 600);
        //            }
        //        } else {
        //            if (data.size() > 0) {
        //                handleNow(data);
        //            }
        //        }
        //        Notification messageNotification = remoteMessage.getNotification();
        //        if (messageNotification != null) {
        //            Log.d(TAG, "Message Notification Body: " + messageNotification.getBody());
        //        }

    }

    @Override
    public void onNewToken(String token) {
        sendRegistrationToServer(token);
    }

    private void handleNow(Map<String, String> data) {
        Log.i(TAG, "handleNow() called with: data = [" + data + "]");
        ZegoUserInfo caller = new ZegoUserInfo();
        caller.userID = data.get("caller_id");
        caller.userName = data.get("caller_name");
        String callID = data.get("call_id");
        String callType = data.get("call_type");
        String callData = data.get("call_data");
        try {
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
                    listener.onReceiveCallInvite(caller, callID, finalType);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showLong(callData);
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
    }
}
