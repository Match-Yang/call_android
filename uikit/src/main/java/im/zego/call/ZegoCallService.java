package im.zego.call;

import android.app.Application;

import im.zego.call.auth.AuthInfoManager;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;

public class ZegoCallService {

    public void init(Application application) {
        AuthInfoManager.getInstance().init(application);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoRoomManager.getInstance().init(appID, application);
    }

    public void login(ZegoUserInfo userInfo, ZegoRoomCallback callback) {
        String token = AuthInfoManager.getInstance().generateLoginToken(userInfo.userID);
        ZegoRoomManager.getInstance().userService.login(userInfo, token, callback);
    }

    public void logout() {
        ZegoRoomManager.getInstance().userService.logout();
    }

    public void uploadLog(final ZegoRoomCallback callback) {
        ZegoRoomManager.getInstance().uploadLog(callback);
    }

    public ZegoUserInfo getLocalUserInfo() {
        return ZegoRoomManager.getInstance().userService.localUserInfo;
    }
}