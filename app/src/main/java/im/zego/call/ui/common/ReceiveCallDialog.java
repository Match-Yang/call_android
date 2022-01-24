package im.zego.call.ui.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.DialogCompat;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.ReceiveCallView.OnReceiveCallViewClickedListener;
import im.zego.call.ui.login.LoginActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.call.utils.PermissionHelper;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMErrorCode;

public class ReceiveCallDialog {

    private ReceiveCallView receiveCallView;
    private boolean isViewAddedToWindow;
    private MediaPlayer mediaPlayer;
    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;
    private CallDialog callDialog;

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
        lp.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
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
                stopRingTone();
            }

            @Override
            public void onAcceptVideoClicked() {
                dismissReceiveCallWindow();
                stopRingTone();
            }

            @Override
            public void onDeclineClicked() {
                dismissReceiveCallWindow();
                stopRingTone();
            }
        });
    }

    public void showReceiveCallWindow() {
        playRingTone();
        if (AppUtils.isAppForeground()) {
            showAppDialog();
        } else {
            if (PermissionHelper.checkFloatWindowPermission()) {
                showGlobalWindow();
            } else {
                //if app is background and receive call, no overlay permission
                // show app dialog android notification
                Activity topActivity = ActivityUtils.getTopActivity();
                PermissionHelper.showFloatPermissionDialog(topActivity, new SimpleCallback() {
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

    private void showAppDialog() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity instanceof LoginActivity) {
            return;
        }
        callDialog = new CallDialog(topActivity, receiveCallView);
        if (!callDialog.isShowing()) {
            callDialog.show();
        }
        callDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopRingTone();
            }
        });
    }

    private void showGlobalWindow() {
        isViewAddedToWindow = true;
        windowManager.addView(receiveCallView, lp);
    }

    public void dismissReceiveCallWindow() {
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
        stopRingTone();
    }

    private void playRingTone() {
        Activity topActivity = ActivityUtils.getTopActivity();
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(topActivity, RingtoneManager.TYPE_RINGTONE);
        mediaPlayer = MediaPlayer.create(topActivity, ringtoneUri);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void stopRingTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void updateData(ZegoUserInfo userInfo, ZegoCallType type) {
        receiveCallView.updateData(userInfo, type);
    }


    static class CallDialog extends Dialog {

        public CallDialog(@NonNull Context context, View view) {
            super(context, R.style.TipsStyle);
            initDialog(view);
        }

        private void initDialog(View view) {
            setCanceledOnTouchOutside(false);
            setCancelable(true);
            setContentView(view);

            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.TOP;
            window.setAttributes(lp);
        }
    }
}
