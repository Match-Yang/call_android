package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoRequestCallback;
import java.util.Map;

public interface ZegoRequestProtocol {

    void request(String path, Map<String, Object> parameter, ZegoRequestCallback callback);
}
