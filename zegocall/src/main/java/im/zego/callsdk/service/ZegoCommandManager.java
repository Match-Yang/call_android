package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoCommand;

public class ZegoCommandManager {

    private static volatile ZegoCommandManager singleton = null;

    private ZegoCommandManager() {
    }

    public static ZegoCommandManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoCommandManager.class) {
                if (singleton == null) {
                    singleton = new ZegoCommandManager();
                }
            }
        }
        return singleton;
    }

    private ZegoRequestProtocol service = new ZegoFirebaseManager();

    public void execute(ZegoCommand command, ZegoRequestCallback callback) {
        service.request(command.getPath(), command.getParameter(), callback);
    }

}
