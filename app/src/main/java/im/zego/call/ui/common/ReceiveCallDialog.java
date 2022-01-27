package im.zego.call.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback;
import im.zego.call.R;
import im.zego.call.ui.common.ReceiveCallView.OnReceiveCallViewClickedListener;
import im.zego.call.ui.login.LoginActivity;
import im.zego.call.utils.PermissionHelper;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;

public class ReceiveCallDialog {

    private ReceiveCallView receiveCallView;
    private boolean isViewAddedToWindow;
    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;
    private Dialog callDialog;
    private OnReceiveCallViewClickedListener listener;
    private AlertDialog floatPermissionDialog;

    public ReceiveCallDialog() {
        Activity topActivity = ActivityUtils.getTopActivity();
        windowManager = (WindowManager) topActivity.getSystemService(Context.WINDOW_SERVICE);
        lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;

        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = 0;

        receiveCallView = new ReceiveCallView(topActivity);
        receiveCallView.setListener(new OnReceiveCallViewClickedListener() {
            @Override
            public void onAcceptAudioClicked() {
                dismissReceiveCallWindow();
                if (listener != null) {
                    listener.onAcceptAudioClicked();
                }
            }

            @Override
            public void onAcceptVideoClicked() {
                dismissReceiveCallWindow();
                if (listener != null) {
                    listener.onAcceptVideoClicked();
                }
            }

            @Override
            public void onDeclineClicked() {
                dismissReceiveCallWindow();
                if (listener != null) {
                    listener.onDeclineClicked();
                }
            }

            @Override
            public void onWindowClicked() {
                dismissReceiveCallWindow();
                if (listener != null) {
                    listener.onWindowClicked();
                }
            }
        });
    }

    public void showReceiveCallWindow() {
        if (AppUtils.isAppForeground()) {
            showAppDialog();
        } else {
            if (PermissionHelper.checkFloatWindowPermission()) {
                showGlobalWindow();
            } else {
                //if app is background and receive call, no overlay permission
                // show app dialog android notification
                Activity topActivity = ActivityUtils.getTopActivity();
                showFloatPermissionDialog(topActivity, new SimpleCallback() {
                    @Override
                    public void onGranted() {
                        showGlobalWindow();
                    }

                    @Override
                    public void onDenied() {
                        showAppDialog();
                    }
                });
            }
        }
    }

    public void showFloatPermissionDialog(Context context, PermissionUtils.SimpleCallback callback) {
        Builder builder = new Builder(context);
        builder.setMessage(R.string.float_permission_tips);
        builder.setPositiveButton(R.string.dialog_login_page_ok, new DialogInterface.OnClickListener() {
            @RequiresApi(api = VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PermissionUtils.requestDrawOverlays(callback);
            }
        });
        floatPermissionDialog = builder.create();
        floatPermissionDialog.setCancelable(false);
        floatPermissionDialog.setCanceledOnTouchOutside(false);
        floatPermissionDialog.show();
    }

    private void showAppDialog() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity instanceof LoginActivity) {
            return;
        }

        callDialog = new CallDialog(topActivity, receiveCallView);
        if (!callDialog.isShowing()) {
            callDialog.show();
        }
    }

    private void showGlobalWindow() {
        isViewAddedToWindow = true;
        windowManager.addView(receiveCallView, lp);
    }

    public void dismissReceiveCallWindow() {
        if (callDialog != null && !callDialog.isShowing()) {
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
        if (floatPermissionDialog != null) {
            floatPermissionDialog.dismiss();
        }
    }

    public void setListener(OnReceiveCallViewClickedListener listener) {
        this.listener = listener;
    }

    public void updateData(ZegoUserInfo userInfo, ZegoCallType type) {
        receiveCallView.updateData(userInfo, type);
    }

    public ZegoUserInfo getUserInfo() {
        return receiveCallView.getUserInfo();
    }


    static class CallDialog extends Dialog {

        public CallDialog(@NonNull Context context, View view) {
            super(context, R.style.TipsStyle);
            initDialog(view);
        }

        private void initDialog(View view) {
            setCanceledOnTouchOutside(false);
            setCancelable(false);
            setContentView(view);

            view.measure(0, 0);

            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.TOP;
            window.setAttributes(lp);
        }
    }
}
