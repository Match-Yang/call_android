package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoCallTimeoutType;
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

    void onReceiveCallTimeout(ZegoCallTimeoutType type);
}
