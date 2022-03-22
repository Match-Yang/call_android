package im.zego.callsdk.command;

public class ZegoEndCallCommand extends ZegoCommand {

    public String fromUserID;
    public String toUserID;

    public ZegoEndCallCommand() {
        super(END_CALL);

    }

}
