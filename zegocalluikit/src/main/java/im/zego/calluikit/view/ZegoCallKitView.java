package im.zego.calluikit.view;

import android.app.Activity;

import android.content.Context;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.ui.common.MinimalDialog;
import im.zego.calluikit.ui.common.MinimalStatus;
import im.zego.calluikit.ui.common.ReceiveCallDialog;
import im.zego.calluikit.ui.common.ReceiveCallView;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;

public class ZegoCallKitView {

    private ReceiveCallDialog receiveCallDialog;
    private MinimalDialog minimalDialog;
    private CallStateManager.CallStateChangedListener callStateChangedListener = (callInfo, before, after) -> {
        minimalDialog.updateRemoteUserInfo(callInfo);
        switch (after) {
            case CallStateManager.TYPE_OUTGOING_CALLING_VOICE:
            case CallStateManager.TYPE_OUTGOING_CALLING_VIDEO:
                minimalDialog.updateStatus(MinimalStatus.Calling);
                break;
            case CallStateManager.TYPE_CONNECTED_VOICE:
            case CallStateManager.TYPE_CONNECTED_VIDEO:
                minimalDialog.updateStatus(MinimalStatus.Connected);
                receiveCallDialog.dismissReceiveCallWindow();
                break;
            case CallStateManager.TYPE_CALL_CANCELED:
                minimalDialog.updateStatus(MinimalStatus.Cancel);
                minimalDialog.reset();
                receiveCallDialog.dismissReceiveCallWindow();
                break;
            case CallStateManager.TYPE_CALL_COMPLETED:
                minimalDialog.updateStatus(MinimalStatus.Ended);
                minimalDialog.reset();
                receiveCallDialog.dismissReceiveCallWindow();
                break;
            case CallStateManager.TYPE_CALL_MISSED:
                minimalDialog.updateStatus(MinimalStatus.Missed);
                minimalDialog.reset();
                receiveCallDialog.dismissReceiveCallWindow();
                break;
            case CallStateManager.TYPE_CALL_DECLINE:
                minimalDialog.updateStatus(MinimalStatus.Decline);
                minimalDialog.reset();
                receiveCallDialog.dismissReceiveCallWindow();
                break;
        }
    };

    public void init(Activity activity) {
        receiveCallDialog = new ReceiveCallDialog(activity);
        minimalDialog = new MinimalDialog(activity);
        CallStateManager.getInstance().addListener(callStateChangedListener);
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