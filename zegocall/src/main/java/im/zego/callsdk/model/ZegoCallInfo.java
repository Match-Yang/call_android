package im.zego.callsdk.model;

import java.util.List;

public class ZegoCallInfo {
    // The ID of the call.
    public String callID;
    // The information of the caller.
    public ZegoUserInfo caller;
    // The information of the callees.
    public List<ZegoUserInfo> callees;

    public ZegoCallType callType;

    @Override
    public String toString() {
        return "ZegoCallInfo{" +
            "callID='" + callID + '\'' +
            ", caller=" + caller +
            ", users=" + callees +
            ", callType=" + callType +
            '}';
    }
}
