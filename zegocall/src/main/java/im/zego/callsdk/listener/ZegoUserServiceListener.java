package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoUserInfo;

/**
 * The listener related to user status.
 * <p>Description: Callbacks that be triggered when in-room user status change.</>
 */
public interface ZegoUserServiceListener {

    /**
     * Callback for changes on user state
     * <p>
     * Description: This callback will be triggered when the state of the user's microphone/camera changes.
     *
     * @param userInfo refers to the changes on user state information
     */
    void onUserInfoUpdated(ZegoUserInfo userInfo);

    void onNetworkQuality(String userID, ZegoNetWorkQuality quality);

    void onReceiveUserError(int errorCode);

    void onReceiveCallingUserDisconnected(ZegoUserInfo userInfo,String callID);
}