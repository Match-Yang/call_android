package im.zego.calluikit.ui.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.blankj.utilcode.util.SizeUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.ui.call.CallStateManager;

public class MinimalDialog {

    private MinimalView minimalView;
    private boolean isViewAddedToWindow;
    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;

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

        LiveEventBus
            .get(Constants.EVENT_MINIMAL, Boolean.class)
            .observeForever(isMinimal -> {
                MinimalView.isShowMinimal = isMinimal;
                if (isMinimal) {
                    showMinimalWindow();
                    minimalView.updateStatus();
                } else {
                    dismissMinimalWindow();
                }
            });
    }

    private void showMinimalWindow() {
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

    public void updateStatus(MinimalStatus next) {
        minimalView.updateStatus(next);
    }

    public void updateRemoteUserInfo(ZegoUserInfo userInfo) {
        minimalView.updateRemoteUserInfo(userInfo);
    }

    public void reset() {
        minimalView.reset();
    }
}