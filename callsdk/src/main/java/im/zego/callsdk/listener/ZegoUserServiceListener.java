package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

/**
 * The listener related to user status.
 * <p>Description: Callbacks that be triggered when in-room user status change.</>
 */
public interface ZegoUserServiceListener {

    void onUserInfoUpdated(ZegoUserInfo userInfo);

    void onCallReceived(ZegoUserInfo userInfo, ZegoCallType type);

    void onCancelCallReceived(ZegoUserInfo userInfo);

    void onCallResponseReceived(ZegoUserInfo userInfo, ZegoResponseType type);

    void onEndCallReceived();

    /**
     * Callbacks related to the user connection status.
     * <p>Description: This callback will be triggered when user gets disconnected due to network error, or gets
     * offline due to the operations in other clients.</>
     *
     * @param state refers to the current connection state.
     * @param event refers to the the event that causes the connection status changes.
     */
    void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event);
}