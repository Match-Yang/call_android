package im.zego.callsdk.service;

import im.zego.callsdk.listener.ZegoDeviceServiceListener;
import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.zegoexpress.constants.ZegoAudioRoute;

public abstract class ZegoDeviceService {

    public ZegoDeviceServiceListener listener;

    private ZegoVideoResolution videoResolution;
    private ZegoAudioBitrate bitrate;
    private boolean noiseSliming;
    private boolean echoCancellation;
    private boolean volumeAdjustment;

    public void setListener(ZegoDeviceServiceListener listener) {
        this.listener = listener;
    }

    public abstract void setDeviceStatus(ZegoDevicesType devicesType, boolean enable);

    public abstract void setVideoResolution(ZegoVideoResolution videoResolution);

    public abstract void setAudioBitrate(ZegoAudioBitrate audioBitrate);

    public abstract void enableCamera(boolean enable);

    public abstract void muteMic(boolean mute);

    public abstract void useFrontCamera(boolean isFront);

    public abstract void enableSpeaker(boolean enable);

    public abstract ZegoAudioRoute getAudioRouteType();

}
