package im.zego.call;

import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;

/**
 * Created by rocket_wang on 2022/3/31.
 */
public interface ZegoCallManagerListener {

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
     * <p>
     * Description: this callback will be triggered when a call has been ended.
     */
    void onReceiveCallEnded();

    /**
     * Callback for a call timed out
     * <p>
     * Description: this callback will be triggered when a call didn't get answered for a long time/ the caller or callee timed out during the call.
     */
    void onReceiveCallTimeout(ZegoUserInfo userInfo, ZegoCallTimeoutType type);
}