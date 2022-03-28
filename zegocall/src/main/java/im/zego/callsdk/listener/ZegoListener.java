package im.zego.callsdk.listener;

import im.zego.callsdk.callback.ZegoNotifyListener;

public interface ZegoListener {

    void addListener(String path, ZegoNotifyListener listener);

    void removeListener(String path, ZegoNotifyListener listener);
}
