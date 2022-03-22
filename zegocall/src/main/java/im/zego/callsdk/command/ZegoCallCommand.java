package im.zego.callsdk.command;

import im.zego.callsdk.model.ZegoCallType;

public class ZegoCallCommand extends ZegoCommand {

    public String fromUserID;
    public String toUserID;
    public String token;
    public ZegoCallType callType;

    public ZegoCallCommand() {
        super(START_CALL);
    }

}
