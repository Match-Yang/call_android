package im.zego.callsdk.service;

import java.util.List;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;

public abstract class ZegoUserService {

    public ZegoUserInfo localUserInfo;
    protected List<ZegoUserInfo> userInfoList;
    protected ZegoUserServiceListener listener;

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    public abstract void login(String authToken, ZegoCallback callback);

    public abstract void logout();

    public abstract void getOnlineUserList(ZegoUserListCallback callback);

    public abstract void validateAccount();
}
