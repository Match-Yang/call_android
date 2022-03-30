package im.zego.callsdk.service;


import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class ZegoDeviceServiceImpl extends ZegoDeviceService {

    public void setVideoResolution(ZegoVideoResolution videoResolution) {
        ZegoVideoConfigPreset configPreset = ZegoVideoConfigPreset.getZegoVideoConfigPreset(videoResolution.value());
        ZegoVideoConfig videoConfig = new ZegoVideoConfig(configPreset);
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
    }

    public void setAudioBitrate(ZegoAudioBitrate audioBitrate) {
        ZegoAudioConfig audioConfig = new ZegoAudioConfig();
        audioConfig.bitrate = audioBitrate.value();
        ZegoExpressEngine.getEngine().setAudioConfig(audioConfig);
    }

    public void setDeviceStatus(ZegoDevicesType devicesType, boolean enable) {
        switch (devicesType) {
            case NOISE_SUPPRESSION:
                ZegoExpressEngine.getEngine().enableANS(enable);
                ZegoExpressEngine.getEngine().enableTransientANS(enable);
                break;
            case ECHO_CANCELLATION:
                ZegoExpressEngine.getEngine().enableAEC(enable);
                break;
            case VOLUME_ADJUSTMENT:
                ZegoExpressEngine.getEngine().enableAGC(enable);
                break;
        }
    }

    public void enableCamera(boolean enable) {
        ZegoExpressEngine.getEngine().enableCamera(enable);
    }

    public void muteMic(boolean mute) {
        ZegoExpressEngine.getEngine().muteMicrophone(mute);
    }

    public void useFrontCamera(boolean isFront) {
        ZegoExpressEngine.getEngine().useFrontCamera(isFront);
    }

    @Override
    public void enableSpeaker(boolean enable) {
        ZegoExpressEngine.getEngine().setAudioRouteToSpeaker(enable);
    }

    @Override
    public void setBestConfig() {

    }

    @Override
    public ZegoAudioRoute getAudioRouteType() {
        return ZegoExpressEngine.getEngine().getAudioRouteType();
    }
}
