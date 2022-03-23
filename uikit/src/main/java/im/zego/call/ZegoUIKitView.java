package im.zego.call;

import im.zego.call.ui.common.MinimalDialog;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.ui.common.ReceiveCallView;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;

/**
 * Created by rocket_wang on 2022/3/19.
 */
public class ZegoUIKitView {
    private ReceiveCallDialog receiveCallDialog;
    private MinimalDialog minimalDialog;

    public void init() {
        receiveCallDialog = new ReceiveCallDialog();

        minimalDialog = new MinimalDialog();
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
}