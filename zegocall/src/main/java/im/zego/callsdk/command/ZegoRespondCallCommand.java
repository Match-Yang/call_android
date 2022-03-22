package im.zego.callsdk.command;

import im.zego.callsdk.model.ZegoResponseType;

public class ZegoRespondCallCommand extends ZegoCommand {

    public String fromUserID;
    public String toUserID;
    public String token;
    public ZegoResponseType responseType;

    public ZegoRespondCallCommand() {
        super(RESPOND_CALL);
    }

}
