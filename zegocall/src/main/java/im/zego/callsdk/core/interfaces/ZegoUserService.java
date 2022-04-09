package im.zego.callsdk.core.interfaces;

import java.util.ArrayList;
import java.util.List;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;

public abstract class ZegoUserService {

    // The delegate instance of the user service.
    public ZegoUserServiceListener listener;
    // The local logged-in user information.
    protected ZegoUserInfo localUserInfo;
    // The online user list.
    public List<ZegoUserInfo> userInfoList = new ArrayList<>();

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    public abstract void getToken(String userID, long effectiveTime, ZegoRequestCallback callback);

    public abstract ZegoUserInfo getLocalUserInfo();

    public abstract void setLocalUser(String userID, String userName);

    public abstract void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state);

    public abstract void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state);
}
