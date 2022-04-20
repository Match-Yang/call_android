package im.zego.calluikit.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SizeUtils;

import com.lzf.easyfloat.EasyFloat;
import im.zego.calluikit.R;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.utils.PermissionHelper;
import im.zego.callsdk.model.ZegoUserInfo;

public class MinimalDialog {

    private MinimalView minimalView;
    private boolean isViewAddedToWindow;
    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;

    private CallStateManager.CallStateChangedListener callStateChangedListener = (callInfo, before, after) -> {
        minimalView.updateRemoteUserInfo(callInfo);
        switch (after) {
            case CallStateManager.TYPE_OUTGOING_CALLING_VOICE:
            case CallStateManager.TYPE_OUTGOING_CALLING_VIDEO:
                minimalView.updateStatus(MinimalStatus.Calling);
                break;
            case CallStateManager.TYPE_CONNECTED_VOICE:
            case CallStateManager.TYPE_CONNECTED_VIDEO:
                minimalView.updateStatus(MinimalStatus.Connected);
                break;
            case CallStateManager.TYPE_CALL_CANCELED:
                minimalView.updateStatus(MinimalStatus.Cancel);
                minimalView.reset();
                break;
            case CallStateManager.TYPE_CALL_COMPLETED:
                minimalView.updateStatus(MinimalStatus.Ended);
                minimalView.reset();
                break;
            case CallStateManager.TYPE_CALL_MISSED:
                minimalView.updateStatus(MinimalStatus.Missed);
                minimalView.reset();
                break;
            case CallStateManager.TYPE_CALL_DECLINE:
                minimalView.updateStatus(MinimalStatus.Decline);
                minimalView.reset();
                break;
        }
        if (CallStateManager.getInstance().isInACallStream()) {
            showGlobalWindow();
        } else {
            dismissMinimalWindow();
        }
    };

    public MinimalDialog(Activity activity) {
        windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM | Gravity.END;

        lp.y = SizeUtils.dp2px(66);

        minimalView = new MinimalView(activity);
        CallStateManager.getInstance().addListener(callStateChangedListener);
    }

    private void showGlobalWindow() {
        if (!isViewAddedToWindow) {
            isViewAddedToWindow = true;
            windowManager.addView(minimalView, lp);
        }
    }

    public void dismissMinimalWindow() {
        if (isViewAddedToWindow) {
            windowManager.removeViewImmediate(minimalView);
            isViewAddedToWindow = false;
        }
        ViewGroup viewParent = (ViewGroup) minimalView.getParent();
        if (viewParent != null) {
            viewParent.removeView(minimalView);
        }
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        minimalView.onUserInfoUpdated(userInfo);
    }
}