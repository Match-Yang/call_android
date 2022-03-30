package im.zego.callsdk.model;

import java.util.List;

public class ZegoCallInfo {
    // The ID of the call.
    public String callID;
    // The information of the caller.
    public ZegoUserInfo caller;
    // The information of the callee.
    public List<ZegoUserInfo> callee;
}
