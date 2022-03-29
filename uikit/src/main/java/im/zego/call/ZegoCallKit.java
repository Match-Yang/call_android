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
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoCallService;
import im.zego.callsdk.service.ZegoDeviceService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.callsdk.utils.ZegoCallHelper;
import im.zego.zegoexpress.ZegoExpressEngine;

public class ZegoCallKit {

    private static final String TAG = "ZegoCallKit";

    private static volatile ZegoCallKit singleton = null;

    private ZegoCallKit() {
        uiKitService = new ZegoUIKitService();
        callView = new ZegoUIKitView();
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

    public final ZegoUIKitService uiKitService;
    private final ZegoUIKitView callView;

    private final CallStateManager.CallStateChangedListener callStateChangedListener = (before, after) -> {
        boolean beforeIsOutgoing = (before == CallStateManager.TYPE_OUTGOING_CALLING_VOICE) ||
                (before == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO);
        boolean beforeIsInComing = (before == CallStateManager.TYPE_INCOMING_CALLING_VOICE) ||
                (before == CallStateManager.TYPE_INCOMING_CALLING_VIDEO);
        boolean afterIsAccept = (after == CallStateManager.TYPE_CONNECTED_VOICE) ||
                (after == CallStateManager.TYPE_CONNECTED_VIDEO);
        if ((beforeIsOutgoing || beforeIsInComing) && afterIsAccept) {
            ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;
            deviceService.enableSpeaker(false);

            String streamID = ZegoCallHelper.getSelfStreamID();
            ZegoExpressEngine.getEngine().startPublishingStream(streamID);
        } else if (after == CallStateManager.TYPE_CALL_CANCELED) {
            ZegoExpressEngine.getEngine().stopPublishingStream();
        } else if (after == CallStateManager.TYPE_CALL_COMPLETED) {
            ZegoExpressEngine.getEngine().stopPublishingStream();
        } else if (after == CallStateManager.TYPE_CALL_MISSED) {
            ZegoExpressEngine.getEngine().stopPublishingStream();
        } else if (after == CallStateManager.TYPE_CALL_DECLINE) {
            ZegoExpressEngine.getEngine().stopPublishingStream();
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper());

    private String CHANNEL_ID = "channel 1";
    private String CHANNEL_NAME = "channel name";
    private String CHANNEL_DESC = "channel desc";
    private int notificationId = 999;

    public void init(Application application) {
        AuthInfoManager.getInstance().init(application);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoServiceManager.getInstance().init(appID, application);
    }

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
                int state;
                if (type == ZegoCallType.Voice) {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VOICE;
                } else {
                    state = CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);
                ZegoCallKit.getInstance().showCallDialog(userInfo, type);
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

        CallStateManager.getInstance().addListener(callStateChangedListener);

        Intent intent = new Intent(activity, ForegroundService.class);
        ContextCompat.startForegroundService(activity, intent);
    }

    public void uploadLog(final ZegoCallback callback) {
        ZegoServiceManager.getInstance().uploadLog(callback);
    }

    public void callUser(ZegoUserInfo userInfo, int callState) {
        CallStateManager.getInstance().setCallState(userInfo, callState);
        CallActivity.startCallActivity(userInfo);
    }

    public ZegoUserInfo getLocalUserInfo() {
        return ZegoServiceManager.getInstance().userService.getLocalUserInfo();
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
        ZegoServiceManager.getInstance().callService.setListener(null);
        ZegoServiceManager.getInstance().userService.setListener(null);
        CallStateManager.getInstance().removeListener(callStateChangedListener);
        activity.stopService(new Intent(activity, ForegroundService.class));
    }

    public void showCallDialog(ZegoUserInfo userInfo, ZegoCallType type) {
        handler.post(() -> {
            callView.updateData(userInfo, type);
            callView.showReceiveCallWindow();
        });
    }
}
