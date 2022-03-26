package im.zego.callsdk.model;

import java.util.List;

public class ZegoCallInfo {
    public String callID;
    public ZegoUserInfo caller;
    public List<ZegoUserInfo> callee;
}
