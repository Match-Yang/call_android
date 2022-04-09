package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoNetWorkQuality;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

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
     * Callback for receive an incoming call
     * <p>
     * Description: This callback will be triggered when receiving an incoming call.
     *
     * @param userInfo refers to the caller information.
     * @param type     indicates the call type.  ZegoCallTypeVoice: Voice call.  ZegoCallTypeVideo: Video call.
     */
    void onReceiveCallInvite(ZegoUserInfo userInfo, ZegoCallType type);

    /**
     * Callback for receive a canceled call
     * <p>
     * Description: This callback will be triggered when the caller cancel the outbound call.
     *
     * @param userInfo refers to the caller information.
     */
    void onReceiveCallCanceled(ZegoUserInfo userInfo, ZegoCancelType cancelType);

    /**
     * call end because of person decline request
     *
     * @param userInfo
     * @param type
     */
    void onReceiveCallResponse(ZegoUserInfo userInfo, ZegoResponseType type);

    /**
     * call end because of room destroy,people hangup(leave room),etc.
     */
    void onReceiveCallEnded();

    /**
     * Callbacks related to the user connection status.
     * <p>Description: This callback will be triggered when user gets disconnected due to network error, or gets
     * offline due to the operations in other clients.</>
     *
     * @param state refers to the current connection state.
     * @param event refers to the the event that causes the connection status changes.
     */
    void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event);

    void onNetworkQuality(String userID, ZegoNetWorkQuality quality);

    void onRoomTokenWillExpire(int remainTimeInSecond, String roomID);
}