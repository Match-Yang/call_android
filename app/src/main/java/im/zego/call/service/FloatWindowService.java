package im.zego.call.service;

import android.app.Activity;
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
import android.widget.Button;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import im.zego.call.service.ReceiveCallView.OnReceiveCallViewClickedListener;
import im.zego.call.ui.call.CallActivity;
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

    private Button button;
    private static final String TAG = "FloatWindowService";
    private ReceiveCallDialog callDialog;
    private ReceiveCallView receiveCallView;
    private boolean isViewAddedToWindow;

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

            }

            @Override
            public void onCallReceived(ZegoUserInfo userInfo, ZegoCallType type) {
                Log.d(TAG, "onCallReceived() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                receiveCallView.updateData(userInfo, type);
                showReceiveCallWindow();
            }

            @Override
            public void onCancelCallReceived(ZegoUserInfo userInfo) {
                dismissReceiveCallWindow();
            }

            @Override
            public void onCallResponseReceived(ZegoUserInfo userInfo, ZegoResponseType type) {
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) topActivity;
                    callActivity.onCallResponseReceived(userInfo, type);
                }
            }

            @Override
            public void onEndCallReceived() {
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity instanceof CallActivity) {
                    CallActivity callActivity = (CallActivity) topActivity;
                    callActivity.onEndCallReceived();
                }
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {

            }
        });
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
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(null);
    }

    private void showReceiveCallWindow() {
        if (AppUtils.isAppForeground()) {
            showAppDialog();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!PermissionUtils.isGrantedDrawOverlays()) {
                    showAppDialog();
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
}