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

    /**
     * Callback for the network quality
     * <p>
     * Description: Callback for the network quality, and this callback will be triggered after the stream publishing or stream playing.     ///
     *
     * @param userID:  refers to the user ID of the stream publisher or stream subscriber.
     * @param quality: refers to the stream quality level.
     */
    void onNetworkQuality(String userID, ZegoNetWorkQuality quality);

    /**
     * Callback when the user was forced to log out
     * <p>
     * Description: this callback will be triggered when the user is logged in from another device.
     *
     * @param errorCode the error type.
     */
    void onReceiveUserError(int errorCode);
}