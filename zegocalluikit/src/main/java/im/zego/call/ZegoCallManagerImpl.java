package im.zego.call;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;

import im.zego.call.auth.AuthInfoManager;
import im.zego.call.service.ForegroundService;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.ReceiveCallView;
import im.zego.call.view.ZegoCallKitView;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoCallService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;

/**
 * Created by rocket_wang on 2022/3/31.
 */
public class ZegoCallManagerImpl {

    private static final String TAG = "ZegoCallManagerImpl";

    public ZegoCallManagerImpl() {
        callView = new ZegoCallKitView();
    }

    // 通用的View：最小化View、呼叫界面弹窗等等
    private final ZegoCallKitView callView;
    private ZegoCallServiceListener listener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String CHANNEL_ID = "channel 1";
    private final String CHANNEL_NAME = "channel name";
    private final String CHANNEL_DESC = "channel desc";
    private final int notificationId = 999;

    public void init(Application application) {
        AuthInfoManager.getInstance().init(application);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoServiceManager.getInstance().init(appID, application);
    }

    public void setListener(ZegoCallServiceListener listener) {
        this.listener = listener;
    }

    /**
     * 启动监听呼叫响应 调用时机：成功登录之后
     */
    public void startListen(Activity activity) {
        callView.init(activity);
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void onUserInfoUpdated(ZegoUserInfo userInfo) {
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) topActivity;
                    callActivity.onUserInfoUpdated(userInfo);
                }
                callView.onUserInfoUpdated(userInfo);
            }

            @Override
            public void onNetworkQuality(String userID, ZegoNetWorkQuality quality) {
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) topActivity;
                    callActivity.onNetworkQuality(userID, quality);
                }
            }

            @Override
            public void onReceiveUserError(int errorCode) {
            }
        });

        callService.setListener(new ZegoCallServiceListener() {
            @Override
            public void onReceiveCallInvite(ZegoUserInfo userInfo, ZegoCallType type) {
                Log.d(TAG, "onReceiveCallInvite() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                int state;
                if (type == ZegoCallType.Voice) {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VOICE;
                } else {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);
                showCallDialog(userInfo, type);
            }

            @Override
            public void onReceiveCallCanceled(ZegoUserInfo userInfo, ZegoCancelType cancelType) {
                CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                callView.dismissReceiveCallWindow();
                dismissNotification(activity);

            }

            @Override
            public void onReceiveCallResponse(ZegoUserInfo userInfo, ZegoResponseType type) {
                if (type == ZegoResponseType.Accept) {
                    int callState = CallStateManager.getInstance().getCallState();
                    if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VOICE) {
                        callState = CallStateManager.TYPE_CONNECTED_VOICE;
                    } else if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO) {
                        callState = CallStateManager.TYPE_CONNECTED_VIDEO;
                    }
                    CallStateManager.getInstance().setCallState(userInfo, callState);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                }
            }

            @Override
            public void onReceiveCallEnded() {
                CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_COMPLETED);
            }

            @Override
            public void onReceiveCallTimeout(ZegoUserInfo userInfo, ZegoCallTimeoutType type) {
                Log.d(TAG, "onReceiveCallTimeout() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_MISSED);
                callView.dismissReceiveCallWindow();
                dismissNotification(activity);
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

    /**
     * 停止监听呼叫响应 调用时机：退出登录之后
     */
    public void stopListen(Activity activity) {
        ZegoServiceManager.getInstance().callService.setListener(null);
        ZegoServiceManager.getInstance().userService.setListener(null);
        activity.stopService(new Intent(activity, ForegroundService.class));
    }

    /**
     * 上传日志
     *
     * @param callback
     */
    public void uploadLog(final ZegoCallback callback) {
        ZegoServiceManager.getInstance().uploadLog(callback);
    }

    /**
     * 主动呼叫用户
     *
     * @param userInfo  用户信息
     * @param callState 呼叫类型，语音/视频
     */
    public void callUser(ZegoUserInfo userInfo, int callState) {
        CallStateManager.getInstance().setCallState(userInfo, callState);
        CallActivity.startCallActivity(userInfo);
    }

    /**
     * 获取本地用户信息
     */
    public ZegoUserInfo getLocalUserInfo() {
        return ZegoServiceManager.getInstance().userService.getLocalUserInfo();
    }

    /**
     * 展示前台服务通知 调用时机：应用切换到后台后
     */
    public void showNotification(ZegoUserInfo userInfo) {
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent();
        try {
            intent = new Intent(topActivity, Class.forName("im.zego.call.ui.login.GoogleLoginActivity"));
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

    /**
     * 隐藏前台服务通知 调用时机：应用切换到前台后
     */
    public void dismissNotification(Activity activity) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        notificationManager.cancel(notificationId);
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

    private void showCallDialog(ZegoUserInfo userInfo, ZegoCallType type) {
        handler.post(() -> {
            callView.updateData(userInfo, type);
            callView.showReceiveCallWindow();
        });
    }

    public void getToken(String userID, long effectiveTime, ZegoRequestCallback callback) {
        ZegoServiceManager.getInstance().userService.getToken(userID, effectiveTime, callback);
    }
}