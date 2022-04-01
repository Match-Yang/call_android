package im.zego.callsdk.command;

import im.zego.callsdk.callback.ZegoRequestCallback;
import java.util.Map;

public interface ZegoRequestProtocol {

    void request(String path, Map<String, Object> parameter, ZegoRequestCallback callback);
}
