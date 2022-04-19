package im.zego.calluikit;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoTokenCallback;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoRoomService;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.listener.ZegoRoomServiceListener;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCallingState;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CallUtils;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.calluikit.ui.call.CallActivity;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.ui.common.ReceiveCallView;
import im.zego.calluikit.view.ZegoCallKitView;

public class ZegoCallManager implements IZegoCallManager {

    private static final String TAG = "ZegoCallManagerImpl";

    private static final class Holder {

        private static final ZegoCallManager INSTANCE = new ZegoCallManager();
    }

    public static ZegoCallManager getInstance() {
        return Holder.INSTANCE;
    }


    private ZegoCallManager() {
        callKitView = new ZegoCallKitView();
    }

    private final ZegoCallKitView callKitView;
    private ZegoCallManagerListener listener;
    private ZegoTokenProvider tokenProvider;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String CHANNEL_ID = "channel 1";
    private final String CHANNEL_NAME = "channel name";
    private final String CHANNEL_DESC = "channel desc";
    private final int notificationId = 999;

    public void init(long appID, @NonNull Application application, ZegoTokenProvider provider) {
        ZegoServiceManager.getInstance().init(appID, application);
        this.tokenProvider = provider;
    }

    public void setListener(ZegoCallManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void uploadLog(ZegoCallback callback) {
        ZegoServiceManager.getInstance().uploadLog(callback);
    }

    /**
     * 启动监听呼叫响应 调用时机：成功登录之后
     */
    public void startListen(Activity activity) {
        callKitView.init(activity);
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoRoomService roomService = ZegoServiceManager.getInstance().roomService;
        roomService.setListener(new ZegoRoomServiceListener() {
            @Override
            public void onRoomTokenWillExpire(int timeInSeconds, String roomID) {
                ZegoTokenProvider tokenProvider = getTokenProvider();
                ZegoUserInfo localUserInfo = getLocalUserInfo();
                if (tokenProvider != null && localUserInfo != null) {
                    tokenProvider.getToken(localUserInfo.userID, new ZegoTokenCallback() {
                        @Override
                        public void onTokenCallback(int errorCode, @Nullable String token) {
                            roomService.renewToken(token, roomID);
                        }
                    });
                }
            }
        });
        userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void onUserInfoUpdated(ZegoUserInfo userInfo) {
                LiveEventBus
                    .get(Constants.EVENT_USER_INFO_UPDATED, ZegoUserInfo.class)
                    .post(userInfo);
                callKitView.onUserInfoUpdated(userInfo);
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
            public void onReceiveCallInvite(ZegoUserInfo userInfo, ZegoCallType type) {
                CallUtils.d("onReceiveCallInvite() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                int state;
                if (type == ZegoCallType.Voice) {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VOICE;
                } else {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);
                showCallDialog(userInfo, type);
                if (listener != null) {
                    listener.onReceiveCallInvite(userInfo, type);
                }
            }

            @Override
            public void onReceiveCallCanceled(ZegoUserInfo userInfo) {
                CallUtils.d("onReceiveCallCanceled() called with: userInfo = [" + userInfo + "]");
                callKitView.dismissReceiveCallWindow();
                CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                dismissNotification(activity);
                if (listener != null) {
                    listener.onReceiveCallCanceled(userInfo);
                }

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
                if (listener != null) {
                    listener.onReceiveCallAccept(userInfo);
                }
            }

            @Override
            public void onReceiveCallDecline(ZegoUserInfo userInfo, ZegoDeclineType declineType) {
                CallUtils.d(
                    "onReceiveCallDecline() called with: userInfo = [" + userInfo + "], declineType = [" + declineType
                        + "]");
                callKitView.dismissReceiveCallWindow();
                if (declineType == ZegoDeclineType.Decline) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_BUSY);

                }
                dismissNotification(activity);
                if (listener != null) {
                    listener.onReceiveCallDecline(userInfo, declineType);
                }
            }

            @Override
            public void onReceiveCallEnded() {
                CallUtils.d("onReceiveCallEnded() called");
                dismissNotification(activity);
                callKitView.dismissReceiveCallWindow();
                CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_CALL_COMPLETED);
                if (listener != null) {
                    listener.onReceiveCallEnded();
                }
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
                callKitView.dismissReceiveCallWindow();
                CallStateManager.getInstance().setCallState(null, callState);
                dismissNotification(activity);
                if (listener != null) {
                    listener.onReceiveCallTimeout(userInfo, type);
                }
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

        createNotificationChannel(activity);
        callKitView.setListener(new ReceiveCallView.OnReceiveCallViewClickedListener() {
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

        AppUtils.registerAppStatusChangedListener(new Utils.OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                dismissNotification(activity);
                for (Activity activity1 : ActivityUtils.getActivityList()) {
                    if (activity1 instanceof CallActivity) {
                        callKitView.dismissReceiveCallWindow();
                        ActivityUtils.startActivity(CallActivity.class);
                        break;
                    }
                }
            }

            @Override
            public void onBackground(Activity activity) {
                boolean needNotification = CallStateManager.getInstance().isInACallStream();
                ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                if (needNotification && userInfo != null) {
                    showNotification(activity, userInfo);
                }
            }
        });

        Intent intent = new Intent(activity, ForegroundService.class);
        ContextCompat.startForegroundService(activity, intent);
    }

    public void stopListen(Activity activity) {
        ZegoServiceManager.getInstance().callService.setListener(null);
        ZegoServiceManager.getInstance().userService.setListener(null);
        activity.stopService(new Intent(activity, ForegroundService.class));
    }

    public void callUser(ZegoUserInfo userInfo, int callState) {
        CallStateManager.getInstance().setCallState(userInfo, callState);
        CallActivity.startCallActivity(userInfo);
    }

    @Override
    public ZegoUserInfo getLocalUserInfo() {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        return userService.getLocalUserInfo();
    }

    private void showNotification(Context context, ZegoUserInfo userInfo) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(CallActivity.USER_INFO, userInfo);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        }
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_dialog_voice_accept)
            .setContentTitle(StringUtils.getString(R.string.app_name))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    private void dismissNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    private void createNotificationChannel(Context context) {
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
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showCallDialog(ZegoUserInfo userInfo, ZegoCallType type) {
        handler.post(() -> {
            callKitView.updateData(userInfo, type);
            callKitView.showReceiveCallWindow();
        });
    }

    public void unInit() {
        ZegoServiceManager.getInstance().unInit();
    }

    @Override
    public void setLocalUser(String userID, String userName) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        userService.setLocalUser(userID, userName);
        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            callKitView.dismissReceiveCallWindow();
            if (callKitView.getContext() != null) {
                dismissNotification(callKitView.getContext());
            }
        }
    }

    public void setTokenProvider(@NonNull ZegoTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public ZegoTokenProvider getTokenProvider() {
        return tokenProvider;
    }
}