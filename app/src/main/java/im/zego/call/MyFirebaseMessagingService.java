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

import android.util.Log;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onCreate() {
        Log.d(TAG,  "onCreate() called");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();

        boolean isAppNotStart = !AppUtils.isAppForeground() && ActivityUtils.getActivityList().isEmpty();
        boolean isDeviceRestart = AppUtils.isAppForeground() && ActivityUtils.getActivityList().isEmpty();
        Log.d(TAG,  "cloud message,isAppNotStart:" + isAppNotStart + ",isDeviceRestart:" + isDeviceRestart);
        if (isAppNotStart || isDeviceRestart) {
            if (data.size() > 0) {
                AppUtils.relaunchApp();
            }
        }
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
