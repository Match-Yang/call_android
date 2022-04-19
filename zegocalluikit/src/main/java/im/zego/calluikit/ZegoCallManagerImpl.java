package im.zego.calluikit;

import static im.zego.calluikit.ui.call.CallActivity.USER_INFO;

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
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.jeremyliao.liveeventbus.LiveEventBus;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCallingState;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CallUtils;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.service.ForegroundService;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.calluikit.ui.call.CallActivity;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.ui.common.ReceiveCallView;
import im.zego.calluikit.view.ZegoCallKitView;

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

    public void init(long appID, Application application) {
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
                //                Activity topActivity = ActivityUtils.getTopActivity();
                //                if (topActivity instanceof CallActivity) {
                //                    CallActivity callActivity = (CallActivity) topActivity;
                //                    callActivity.onUserInfoUpdated(userInfo);
                //                }
                LiveEventBus
                    .get(Constants.EVENT_USER_INFO_UPDATED, ZegoUserInfo.class)
                    .post(userInfo);
                callView.onUserInfoUpdated(userInfo);
            }

            @Override
            public void onNetworkQuality(String userID, ZegoNetWorkQuality quality) {
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    ((CallActivity) topActivity).onNetworkQuality(userID, quality);
                }
            }
        });

        callService.setListener(new ZegoCallServiceListener() {
            @Override
            public void onReceiveCallInvite(ZegoUserInfo userInfo, String callID, ZegoCallType type) {
                CallUtils.d("onReceiveCallInvite() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
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
                CallUtils.d(
                    "onReceiveCallCanceled() called with: userInfo = [" + userInfo + "], cancelType = [" + cancelType
                        + "]");
                callView.dismissReceiveCallWindow();
                CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                dismissNotification(activity);

            }

            @Override
            public void onReceiveCallAccept(ZegoUserInfo userInfo) {
                CallUtils.d("onReceiveCallAccept() called with: userInfo = [" + userInfo + "]");
                int callState = CallStateManager.getInstance().getCallState();
                if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VOICE) {
                    callState = CallStateManager.TYPE_CONNECTED_VOICE;
                } else if (callState == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO) {
                    callState = CallStateManager.TYPE_CONNECTED_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, callState);
            }

            @Override
            public void onReceiveCallDecline(ZegoUserInfo userInfo, ZegoDeclineType declineType) {
                CallUtils.d(
                    "onReceiveCallDecline() called with: userInfo = [" + userInfo + "], declineType = [" + declineType
                        + "]");
                callView.dismissReceiveCallWindow();
                if (declineType == ZegoDeclineType.Decline) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_BUSY);

                }
                dismissNotification(activity);
            }

            @Override
            public void onReceiveCallEnded() {
                CallUtils.d("onReceiveCallEnded() called");
                CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_COMPLETED);
            }

            @Override
            public void onReceiveCallTimeout(ZegoUserInfo userInfo, ZegoCallTimeoutType type) {
                CallUtils.d("onReceiveCallTimeout() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                int callState;
                if (type == ZegoCallTimeoutType.Calling) {
                    callState = CallStateManager.TYPE_CALL_MISSED;
                } else {
                    callState = CallStateManager.TYPE_CALL_COMPLETED;
                }
                callView.dismissReceiveCallWindow();
                CallStateManager.getInstance().setCallState(null, callState);
                dismissNotification(activity);
            }

            @Override
            public void onCallingStateUpdated(ZegoCallingState state) {
                Log.d(TAG, "onCallingStateUpdated() called with: state = [" + state + "]");
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof BaseActivity) {
                    ((BaseActivity<?>) topActivity).onCallingStateUpdated(state);
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

        FirebaseAuth.getInstance().addAuthStateListener(new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    callView.dismissReceiveCallWindow();
                    dismissNotification(activity);
                }
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
        Intent intent = new Intent(topActivity, CallActivity.class);
        intent.putExtra(USER_INFO, userInfo);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent
            .getActivity(topActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    public void dismissCallDialog() {
        handler.post(callView::dismissReceiveCallWindow);
    }

    public void getToken(String userID, long effectiveTime, ZegoRequestCallback callback) {
        ZegoServiceManager.getInstance().userService.getToken(userID, effectiveTime, callback);
    }

    public void unInit() {
        ZegoServiceManager.getInstance().unInit();
    }
}