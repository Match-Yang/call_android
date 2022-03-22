package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoUserLisCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.List;

public abstract class ZegoUserService {

    public ZegoUserInfo localUserInfo;
    private List<ZegoUserInfo> userInfoList;
    private ZegoUserServiceListener listener;

    public abstract void login(String authToken, ZegoCallback callback);

    public abstract void logout();

    public abstract void getOnlineUserList(ZegoUserLisCallback callback);

    public abstract void validateAccount();
}
