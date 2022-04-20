package im.zego.calluikit.view;

import android.app.Activity;

import android.content.Context;
import im.zego.calluikit.ui.common.MinimalDialog;
import im.zego.calluikit.ui.common.ReceiveCallDialog;
import im.zego.calluikit.ui.common.ReceiveCallView;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;

public class ZegoCallKitView {

    private ReceiveCallDialog receiveCallDialog;
    private MinimalDialog minimalDialog;

    public void init(Activity activity) {
        receiveCallDialog = new ReceiveCallDialog(activity);
        minimalDialog = new MinimalDialog(activity);
    }

    public void updateData(ZegoUserInfo userInfo, ZegoCallType type) {
        if (receiveCallDialog != null) {
            receiveCallDialog.updateData(userInfo, type);
        }
    }

    public void dismissReceiveCallWindow() {
        if (receiveCallDialog != null) {
            receiveCallDialog.dismissReceiveCallWindow();
        }
    }

    public void showReceiveCallWindow() {
        if (receiveCallDialog != null) {
            receiveCallDialog.showReceiveCallWindow();
        }

    }

    public void setListener(ReceiveCallView.OnReceiveCallViewClickedListener listener) {
        receiveCallDialog.setListener(listener);
    }

    public Context getContext() {
        if (receiveCallDialog != null) {
            return receiveCallDialog.getContext();
        } else {
            return null;
        }
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        if (minimalDialog != null) {
            minimalDialog.onUserInfoUpdated(userInfo);
        }
    }
}