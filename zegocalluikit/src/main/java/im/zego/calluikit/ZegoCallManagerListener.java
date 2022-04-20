package im.zego.calluikit;

import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoUserInfo;

public interface ZegoCallManagerListener {

    void onReceiveCallingUserDisconnected(ZegoUserInfo userInfo);

    void onReceiveCallInvite(ZegoUserInfo userInfo, ZegoCallType callType);

    void onReceiveCallCanceled(ZegoUserInfo userInfo);

    void onReceiveCallAccept(ZegoUserInfo userInfo);

    void onReceiveCallDecline(ZegoUserInfo userInfo, ZegoDeclineType declineType);

    void onReceiveCallEnded();

    void onReceiveCallTimeout(ZegoUserInfo userInfo, ZegoCallTimeoutType type);
}