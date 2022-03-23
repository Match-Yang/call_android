package im.zego.call;

import im.zego.call.ui.call.CallStateManager;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.service.ZegoServiceManager;

public class ZegoUIKitService {

    public void login(String authToken, ZegoCallback callback) {
        ZegoServiceManager.getInstance().userService.login(authToken, callback);
    }

    public void logout() {
        ZegoServiceManager.getInstance().userService.logout();
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
    }

    public void validateAccount() {
        ZegoServiceManager.getInstance().userService.validateAccount();
    }
}