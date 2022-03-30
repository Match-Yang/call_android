package im.zego.callsdk.listener;

import im.zego.zegoexpress.constants.ZegoAudioRoute;

public interface ZegoDeviceServiceListener {
    /**
     * Callback for the audio output route changed
     * <p>
     * Description: this callback will be triggered when switching the audio output between speaker, receiver, and bluetooth headset.
     *
     * @param audioRoute the device type of audio output.
     */
    void onAudioRouteChange(ZegoAudioRoute audioRoute);
}