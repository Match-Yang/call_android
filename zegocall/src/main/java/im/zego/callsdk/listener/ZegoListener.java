package im.zego.callsdk.listener;

import im.zego.callsdk.callback.ZegoRequestCallback;

public interface ZegoListener {

    void registerListener(Object listener, String path, ZegoRequestCallback callback);

    void removeListener(Object listener, String path);
}
