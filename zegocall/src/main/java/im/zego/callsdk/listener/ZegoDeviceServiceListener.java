package im.zego.callsdk.listener;

import im.zego.zegoexpress.constants.ZegoAudioRoute;

public interface ZegoDeviceServiceListener {
    public void onAudioRouteChange(ZegoAudioRoute audioRoute);
}