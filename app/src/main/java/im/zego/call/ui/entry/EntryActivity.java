package im.zego.call.ui.entry;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.Utils.OnAppStatusChangedListener;
import im.zego.call.R;
import im.zego.call.databinding.ActivityEntryBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.ui.common.ReceiveCallView.OnReceiveCallViewClickedListener;
import im.zego.call.ui.login.LoginActivity;
import im.zego.call.ui.setting.SettingActivity;
import im.zego.call.ui.user.OnlineUserActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.call.utils.PermissionHelper;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class EntryActivity extends BaseActivity<ActivityEntryBinding> {

    public static final String URL_GET_MORE = "https://www.zegocloud.com/";
    public static final String URL_CONTACT_US = "https://www.zegocloud.com/talk";
    private static final String TAG = "EntryActivity";
    private ReceiveCallDialog dialog;
    private String CHANNEL_ID = "channel 1";
    private String CHANNEL_NAME = "channel name";
    private String CHANNEL_DESC = "channel desc";
    private int notificationId = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.entrySetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(SettingActivity.class);
            }
        });
        binding.entryContactUs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_CONTACT_US);
            }
        });
        binding.entryGetMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_GET_MORE);
            }
        });
        binding.entryBannerCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(OnlineUserActivity.class);
            }
        });

        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        ZegoUserInfo localUserInfo = userService.localUserInfo;

        binding.entryUserId.setText("ID:" + localUserInfo.userID);
        binding.entryUserName.setText(localUserInfo.userName);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(localUserInfo.userName);
        binding.entryUserAvatar.setImageDrawable(userIcon);

        dialog = new ReceiveCallDialog();
        userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void onUserInfoUpdated(ZegoUserInfo userInfo) {
                Log.d(TAG, "onUserInfoUpdated() called with: userInfo = [" + userInfo + "]");
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) topActivity;
                    callActivity.onUserInfoUpdated(userInfo);
                }
            }

            @Override
            public void onReceiveCallInvite(ZegoUserInfo userInfo, ZegoCallType type) {
                Log.d(TAG, "onCallReceived() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                boolean needNotification = CallStateManager.getInstance().needNotification();
                if (needNotification) {
                    //needNotification means call is happening,reject other calls
                    userService.respondCall(ZegoResponseType.Reject, userInfo.userID, null, errorCode -> {

                    });
                    return;
                }
                dialog.updateData(userInfo, type);
                int state;
                if (type == ZegoCallType.VOICE) {
                    state = CallStateManager.TYPE_INCOMING_CALLING_AUDIO;
                } else {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);

                dialog.showReceiveCallWindow();

                //show notification on lock-screen
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                boolean isScreenOff = !powerManager.isInteractive();
                boolean isBackground = !AppUtils.isAppForeground();
                boolean hasOverlayPermission = PermissionHelper.checkFloatWindowPermission();
                if (isScreenOff || (isBackground && !hasOverlayPermission)) {
                    showNotification(userInfo);
                }
            }

            @Override
            public void onReceiveCallCanceled(ZegoUserInfo userInfo,ZegoCancelType cancelType) {
                if (cancelType == ZegoCancelType.INTENT) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_MISSED);
                }
                dialog.dismissReceiveCallWindow();
            }

            @Override
            public void onReceiveCallResponse(ZegoUserInfo userInfo, ZegoResponseType type) {
                if (type == ZegoResponseType.Reject) {
                    userService.endCall(errorCode -> {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                    });
                } else {
                    int callState = CallStateManager.getInstance().getCallState();
                    if (callState == CallStateManager.TYPE_OUTGOING_CALLING_AUDIO) {
                        callState = CallStateManager.TYPE_CONNECTED_VOICE;
                    } else if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO) {
                        callState = CallStateManager.TYPE_CONNECTED_VIDEO;
                    }
                    CallStateManager.getInstance().setCallState(userInfo, callState);
                }
            }

            @Override
            public void onReceiveCallEnded() {
                Log.d(TAG, "onEndCallReceived() called");
                userService.endCall(errorCode -> {
                    CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_COMPLETED);
                });
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {

            }
        });

        createNotificationChannel();
        AppUtils.registerAppStatusChangedListener(new OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                dismissNotification(EntryActivity.this, notificationId);
            }

            @Override
            public void onBackground(Activity activity) {
                boolean needNotification = CallStateManager.getInstance().needNotification();
                ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                if (needNotification && userInfo != null) {
                    showNotification(userInfo);
                }
            }
        });
        dialog.setListener(new OnReceiveCallViewClickedListener() {
            @Override
            public void onAcceptAudioClicked() {
                dismissNotification(EntryActivity.this, notificationId);
            }

            @Override
            public void onAcceptVideoClicked() {
                dismissNotification(EntryActivity.this, notificationId);
            }

            @Override
            public void onDeclineClicked() {
                dismissNotification(EntryActivity.this, notificationId);
            }

            @Override
            public void onWindowClicked() {
                CallActivity.startCallActivity(dialog.getUserInfo());
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            Activity topActivity = ActivityUtils.getTopActivity();
            NotificationManager notificationManager = topActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    void showNotification(ZegoUserInfo userInfo) {
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent(topActivity, LoginActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(topActivity, 0, intent, 0);

        NotificationCompat.Builder builder = new Builder(topActivity, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_dialog_voice_accept)
            .setContentTitle(topActivity.getString(R.string.app_name))
            .setContentText(topActivity.getString(R.string.call_notification, userInfo.userName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(topActivity);
        notificationManager.notify(notificationId, builder.build());
    }

    void dismissNotification(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(null);
    }

    @Override
    public void onBackPressed() {

    }
}