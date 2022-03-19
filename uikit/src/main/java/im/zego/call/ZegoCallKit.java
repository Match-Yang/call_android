package im.zego.call;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.Objects;

import im.zego.call.service.ForegroundService;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.ReceiveCallView;
import im.zego.call.utils.PermissionHelper;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class ZegoCallKit {
    private static final String TAG = "ZegoCallKit";

    private static volatile ZegoCallKit singleton = null;

    private ZegoCallKit() {
        callService = new ZegoCallService();
        callView = new ZegoCallView();
    }

    public static ZegoCallKit getInstance() {
        if (singleton == null) {
            synchronized (ZegoCallKit.class) {
                if (singleton == null) {
                    singleton = new ZegoCallKit();
                }
            }
        }
        return singleton;
    }

    private final ZegoCallService callService;
    private final ZegoCallView callView;

    private String CHANNEL_ID = "channel 1";
    private String CHANNEL_NAME = "channel name";
    private String CHANNEL_DESC = "channel desc";
    private int notificationId = 999;

    public void init(Application application) {
        callService.init(application);
    }

    public void startListen(Activity activity) {
        callView.init();
        ZegoUserInfo localUserInfo = ZegoCallKit.getInstance().getLocalUserInfo();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
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
                Activity topActivity = ActivityUtils.getTopActivity();
                Log.d(TAG,
                        "onReceiveCallInvite() called with: userInfo = [" + userInfo + "], topActivity = [" + topActivity
                                + "]");
                boolean inACallStream = CallStateManager.getInstance().isInACallStream();
                if (inACallStream || topActivity instanceof CallActivity) {
                    // means call is happening,reject other calls
                    userService.respondCall(ZegoResponseType.Reject, userInfo.userID, null, errorCode -> {

                    });
                    return;
                }
                callView.updateData(userInfo, type);
                int state;
                if (type == ZegoCallType.Voice) {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VOICE;
                } else {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);

                //show notification on lock-screen
                PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOff = !powerManager.isInteractive();
                boolean isBackground = !AppUtils.isAppForeground();
                boolean hasOverlayPermission = PermissionHelper.checkFloatWindowPermission();
                if (isScreenOff || (isBackground && !hasOverlayPermission)) {
                    showNotification(userInfo);
                }
                callView.showReceiveCallWindow();
            }

            @Override
            public void onReceiveCallCanceled(ZegoUserInfo userInfo, ZegoCancelType cancelType) {
                Log.d(TAG,
                        "onReceiveCallCanceled() called with: userInfo = [" + userInfo + "], cancelType = [" + cancelType
                                + "]");
                boolean connected = CallStateManager.getInstance().isConnected();
                ZegoUserInfo userInfo1 = CallStateManager.getInstance().getUserInfo();
                if (connected && userInfo1 != userInfo) {
                    return;
                }
                if (cancelType == ZegoCancelType.INTENT) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_MISSED);
                }
                callView.dismissReceiveCallWindow();
                dismissNotification(activity);
            }

            @Override
            public void onReceiveCallResponse(ZegoUserInfo userInfo, ZegoResponseType type) {
                Log.d(TAG, "onReceiveCallResponse() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                boolean connected = CallStateManager.getInstance().isConnected();
                ZegoUserInfo userInfo1 = CallStateManager.getInstance().getUserInfo();
                if (connected && userInfo1 != userInfo) {
                    return;
                }
                if (type == ZegoResponseType.Reject) {
                    userService.endCall(errorCode -> {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                    });
                } else {
                    int callState = CallStateManager.getInstance().getCallState();
                    if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VOICE) {
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
                    int callState = CallStateManager.getInstance().getCallState();
                    if (callState == CallStateManager.TYPE_CONNECTED_VIDEO ||
                            callState == CallStateManager.TYPE_CONNECTED_VOICE) {
                        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_COMPLETED);
                    } else {
                        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_CANCELED);
                    }
                });
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {
                if (event == ZIMConnectionEvent.KICKED_OUT) {
                    ToastUtils.showShort(R.string.toast_kickout_error);
                    logout();
                    return;
                }
                if (state == ZIMConnectionState.DISCONNECTED) {
                    logout();
                } else if (state == ZIMConnectionState.CONNECTED) {
                    Activity topActivity = ActivityUtils.getTopActivity();
                    if (topActivity instanceof CallActivity) {
                        CallActivity callActivity = (CallActivity) topActivity;
                        callActivity.onConnectionStateChanged(state, event);
                    }
                } else {
                    Activity topActivity = ActivityUtils.getTopActivity();
                    if (topActivity instanceof CallActivity) {
                        CallActivity callActivity = (CallActivity) topActivity;
                        callActivity.onConnectionStateChanged(state, event);
                    }
                }
            }

            @Override
            public void onNetworkQuality(String userID, ZegoNetWorkQuality quality) {
                if (Objects.equals(userID, localUserInfo.userID) || userID == null) {
                    Activity topActivity = ActivityUtils.getTopActivity();
                    if (topActivity instanceof CallActivity) {
                        CallActivity callActivity = (CallActivity) topActivity;
                        callActivity.onNetworkQuality(userID, quality);
                    }
                }
            }
        });

        createNotificationChannel();
        callView.setListener(new ReceiveCallView.OnReceiveCallViewClickedListener() {
            @Override
            public void onAcceptAudioClicked() {
                dismissNotification(activity);
            }

            @Override
            public void onAcceptVideoClicked() {
                dismissNotification(activity);
            }

            @Override
            public void onDeclineClicked() {
                dismissNotification(activity);
            }

            @Override
            public void onWindowClicked() {
                dismissNotification(activity);
            }
        });

        Intent intent = new Intent(activity, ForegroundService.class);
        ContextCompat.startForegroundService(activity, intent);
    }

    public void login(ZegoUserInfo userInfo, ZegoRoomCallback callback) {
        callService.login(userInfo, callback);
    }

    public void logout() {
        callService.logout();
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
    }

    public void uploadLog(final ZegoRoomCallback callback) {
        callService.uploadLog(callback);
    }

    public void callUser(ZegoUserInfo userInfo, int callState) {
        CallStateManager.getInstance().setCallState(userInfo, callState);
        CallActivity.startCallActivity(userInfo);
    }

    public ZegoUserInfo getLocalUserInfo() {
        return callService.getLocalUserInfo();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            Activity topActivity = ActivityUtils.getTopActivity();
            NotificationManager notificationManager = topActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(ZegoUserInfo userInfo) {
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent();
        try {
            intent = new Intent(topActivity, Class.forName("im.zego.call.ui.login.LoginActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(topActivity, 0, intent, 0);

        String notificationText = StringUtils.getString(R.string.call_notification, userInfo.userName);
        int callState = CallStateManager.getInstance().getCallState();
        if (callState == CallStateManager.TYPE_INCOMING_CALLING_VIDEO ||
                callState == CallStateManager.TYPE_INCOMING_CALLING_VOICE) {
            notificationText = StringUtils.getString(R.string.receive_call_notification, userInfo.userName);
        } else if (callState == CallStateManager.TYPE_CONNECTED_VIDEO ||
                callState == CallStateManager.TYPE_CONNECTED_VOICE) {
            notificationText = StringUtils.getString(R.string.call_notification, userInfo.userName);
        } else if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO ||
                callState == CallStateManager.TYPE_OUTGOING_CALLING_VOICE) {
            notificationText = StringUtils.getString(R.string.request_call_notification, userInfo.userName);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(topActivity, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_dialog_voice_accept)
                .setContentTitle(StringUtils.getString(R.string.app_name))
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(topActivity);
        Notification build = builder.build();
        build.defaults = Notification.DEFAULT_SOUND;

        notificationManager.notify(notificationId, build);
    }

    public void dismissNotification(Activity activity) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        notificationManager.cancel(notificationId);
    }

    public void stopListen(Activity activity) {
        ZegoRoomManager.getInstance().userService.setListener(null);
        activity.stopService(new Intent(activity, ForegroundService.class));
    }
}
