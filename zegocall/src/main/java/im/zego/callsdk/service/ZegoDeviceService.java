package im.zego.callsdk.service;

import android.view.TextureView;
import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;

public abstract class ZegoDeviceService {

    private ZegoDeviceServiceListener listener;

    private ZegoVideoResolution videoResolution;
    private ZegoAudioBitrate bitrate;
    private boolean noiseSliming;
    private boolean echoCancellation;
    private boolean volumeAdjustment;

    public abstract void setDeviceStatus(ZegoDevicesType devicesType, boolean enable);

    public abstract void setVideoResolution(ZegoVideoResolution videoResolution);

    public abstract void setAudioBitrate(ZegoAudioBitrate audioBitrate);

    public abstract void enableCamera(boolean enable);

    public abstract void muteMic(boolean enable);

    public abstract void useFrontCamera(boolean isFront);

    public abstract void enableSpeaker(boolean enable);

    public abstract void playVideoStream(String userID, TextureView textureView);

    public abstract void stopPlayStream(String userID);

}
