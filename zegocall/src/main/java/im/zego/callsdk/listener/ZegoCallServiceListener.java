package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCallingState;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoUserInfo;

public interface ZegoCallServiceListener {

    /**
     * Callback for receive an incoming call
     * <p>
     * Description: This callback will be triggered when receiving an incoming call.
     *
     * @param userInfo refers to the caller information.
     * @param type     indicates the call type.  ZegoCallTypeVoice: Voice call.  ZegoCallTypeVideo: Video call.
     */
    void onReceiveCallInvite(ZegoUserInfo userInfo, String callID, ZegoCallType type);

    /**
     * Callback for receive a canceled call
     * <p>
     * Description: This callback will be triggered when the caller cancel the outbound call.
     *
     * @param userInfo refers to the caller information.
     */
    void onReceiveCallCanceled(ZegoUserInfo userInfo, ZegoCancelType cancelType);


    void onReceiveCallAccept(ZegoUserInfo userInfo);

    void onReceiveCallDecline(ZegoUserInfo userInfo, ZegoDeclineType declineType);

    /**
     * call end because of room destroy,people hangup(leave room),etc.
     * <p>
     * Description: this callback will be triggered when a call has been ended.
     */
    void onReceiveCallEnded();

    /**
     * Callback for a call timed out
     * <p>
     * Description: this callback will be triggered when a call didn't get answered for a long time/ the caller or
     * callee timed out during the call.
     */
    void onReceiveCallTimeout(ZegoUserInfo userInfo, ZegoCallTimeoutType type);

    void onCallingStateUpdated(ZegoCallingState state);
}
