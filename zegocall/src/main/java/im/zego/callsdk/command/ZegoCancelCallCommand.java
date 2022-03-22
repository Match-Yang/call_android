package im.zego.callsdk.command;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.service.ZegoCommandManager;

public class ZegoCancelCallCommand extends ZegoCommand {

    public String fromUserID;
    public String toUserID;

    public ZegoCancelCallCommand() {
        super(CANCEL_CALL);

    }
}
