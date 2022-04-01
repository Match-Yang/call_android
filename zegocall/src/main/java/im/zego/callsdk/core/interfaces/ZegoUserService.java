package im.zego.callsdk.core.interfaces;

import im.zego.callsdk.callback.ZegoRequestCallback;
import java.util.List;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;

public abstract class ZegoUserService {

    // The delegate instance of the user service.
    protected ZegoUserServiceListener listener;
    // The local logged-in user information.
    protected ZegoUserInfo localUserInfo;
    // The online user list.
    protected List<ZegoUserInfo> userInfoList;

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    /**
     * User to log in
     * <p>
     * Description: this method can be used to log in to the Call service.
     * <p>
     * android.telecom.Call this method at: after the SDK initialization
     * <p>
     * - Parameter callback: refers to the callback for log in.
     */
    public abstract void login(String authToken, ZegoCallback callback);

    /**
     * User to log out
     * <p>
     * Description: this method can be used to log out from the current user account.
     * <p>
     * Call this method at: after the user login
     */
    public abstract void logout();

    /**
     * Get the online user list
     * <p>
     * Description: this method can be used to get the current online user list.
     * <p>
     * Call this method at: after the SDK initialization
     */
    public abstract void getOnlineUserList(ZegoUserListCallback callback);

    public abstract void getToken(String userID, long effectiveTime, ZegoRequestCallback callback);

    public abstract ZegoUserInfo getLocalUserInfo();

    public abstract void setLocalUser(String userID, String userName);

    public abstract void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state);

    public abstract void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state);
}
