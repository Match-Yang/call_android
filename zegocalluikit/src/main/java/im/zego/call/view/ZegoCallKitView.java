package im.zego.call.view;

import android.app.Activity;

import im.zego.call.ui.common.MinimalDialog;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.ui.common.ReceiveCallView;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;

public class ZegoCallKitView {
    private ReceiveCallDialog receiveCallDialog;
    private MinimalDialog minimalDialog;

    public void init(Activity activity) {
        receiveCallDialog = new ReceiveCallDialog(activity);

        minimalDialog = new MinimalDialog(activity);
        minimalDialog.showMinimalWindow();
    }

    public void updateData(ZegoUserInfo userInfo, ZegoCallType type) {
        receiveCallDialog.updateData(userInfo, type);
    }

    public void dismissReceiveCallWindow() {
        receiveCallDialog.dismissReceiveCallWindow();
    }

    public void showReceiveCallWindow() {
        receiveCallDialog.showReceiveCallWindow();

    }
    public void setListener(ReceiveCallView.OnReceiveCallViewClickedListener listener) {
        receiveCallDialog.setListener(listener);

    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        minimalDialog.onUserInfoUpdated(userInfo);
    }
}