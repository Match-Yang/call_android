package im.zego.call.service;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils.OnAppStatusChangedListener;
import im.zego.call.R;
import im.zego.call.service.ReceiveCallView.OnReceiveCallViewClickedListener;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.ui.login.LoginActivity;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class FloatWindowService extends Service {

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;

    private static final String TAG = "FloatWindowService";
    private ReceiveCallDialog callDialog;
    private ReceiveCallView receiveCallView;
    private boolean isViewAddedToWindow;
    private OnAppStatusChangedListener appStatusChangedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = 0;

        receiveCallView = new ReceiveCallView(this);
        receiveCallView.setListener(new OnReceiveCallViewClickedListener() {
            @Override
            public void onAcceptAudioClicked() {
                dismissReceiveCallWindow();
            }

            @Override
            public void onAcceptVideoClicked() {
                dismissReceiveCallWindow();
            }

            @Override
            public void onDeclineClicked() {
                dismissReceiveCallWindow();
            }
        });

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
            public void onCallReceived(ZegoUserInfo userInfo, ZegoCallType type) {
                Log.d(TAG, "onCallReceived() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                boolean needNotification = CallStateManager.getInstance().needNotification();
                if (needNotification) {
                    userService.responseCall(ZegoResponseType.Decline, userInfo.userID, null, errorCode -> {

                    });
                }

                receiveCallView.updateData(userInfo, type);
                int state;
                if (type == ZegoCallType.Audio) {
                    state = CallStateManager.TYPE_CONNECTED_VOICE;
                } else {
                    state = CallStateManager.TYPE_CONNECTED_VIDEO;
                }
                CallStateManager.getInstance().setCallState(userInfo, state);
                showReceiveCallWindow();
            }

            @Override
            public void onCancelCallReceived(ZegoUserInfo userInfo) {
                CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                dismissReceiveCallWindow();
            }

            @Override
            public void onCallResponseReceived(ZegoUserInfo userInfo, ZegoResponseType type) {
                if (type == ZegoResponseType.Decline) {
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
            public void onEndCallReceived() {
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
        appStatusChangedListener = new OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
                notificationManager.cancel(notificationId);
            }

            @Override
            public void onBackground(Activity activity) {
                boolean needNotification = CallStateManager.getInstance().needNotification();
                ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                if (needNotification && userInfo != null) {
                    showNotification(userInfo);
                }
            }
        };
        AppUtils.registerAppStatusChangedListener(appStatusChangedListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(null);
        AppUtils.unregisterAppStatusChangedListener(appStatusChangedListener);
    }

    private void showReceiveCallWindow() {
        if (AppUtils.isAppForeground()) {
            showAppDialog();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!PermissionUtils.isGrantedDrawOverlays()) {
                    //if app is background and receive call, no overlay permission
                    // show app dialog android notification
                    showAppDialog();
                    ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                    if (userInfo != null) {
                        showNotification(userInfo);
                    }
                    return;
                }
            }
            showGlobalWindow();
        }
    }

    private void showAppDialog() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity instanceof LoginActivity) {
            return;
        }
        callDialog = new ReceiveCallDialog(topActivity, receiveCallView);
        if (!callDialog.isShowing()) {
            callDialog.show();
        }
    }

    private void showGlobalWindow() {
        isViewAddedToWindow = true;
        windowManager.addView(receiveCallView, lp);
    }

    private void dismissReceiveCallWindow() {
        if (callDialog != null) {
            callDialog.dismiss();
        }
        if (isViewAddedToWindow) {
            windowManager.removeViewImmediate(receiveCallView);
            isViewAddedToWindow = false;
        }
        ViewGroup viewParent = (ViewGroup) receiveCallView.getParent();
        if (viewParent != null) {
            viewParent.removeView(receiveCallView);
        }
    }

    private String CHANNEL_ID = "channel 1";
    private String CHANNEL_NAME = "channel name";
    private String CHANNEL_DESC = "channel desc";
    private int notificationId = 999;

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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void showNotification(ZegoUserInfo userInfo) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_setting_pressed)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.call_notification, userInfo.userName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }
}