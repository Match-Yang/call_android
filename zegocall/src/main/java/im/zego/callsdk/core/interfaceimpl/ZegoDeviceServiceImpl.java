package im.zego.callsdk.core.interfaceimpl;


import android.util.Log;

import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.callsdk.core.interfaces.ZegoDeviceService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.utils.CoreTest;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoCapturePipelineScaleMode;
import im.zego.zegoexpress.constants.ZegoTrafficControlFocusOnMode;
import im.zego.zegoexpress.constants.ZegoTrafficControlMinVideoBitrateMode;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
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
        Log.d(CoreTest.TAG, "enableCamera() called with: enable = [" + enable + "]");
        ZegoExpressEngine.getEngine().enableCamera(enable);
        ZegoServiceManager.getInstance().userService.getLocalUserInfo().camera = enable;
        if (ZegoServiceManager.getInstance().userService.listener != null) {
            ZegoServiceManager.getInstance().userService.listener.onUserInfoUpdated(ZegoServiceManager.getInstance().userService.getLocalUserInfo());
        }
    }

    public void enableMic(boolean enable) {
        ZegoExpressEngine.getEngine().muteMicrophone(!enable);
        ZegoServiceManager.getInstance().userService.getLocalUserInfo().mic = enable;
        if (ZegoServiceManager.getInstance().userService.listener != null) {
            ZegoServiceManager.getInstance().userService.listener.onUserInfoUpdated(ZegoServiceManager.getInstance().userService.getLocalUserInfo());
        }
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
        ZegoExpressEngine.getEngine().enableHardwareEncoder(true);
        ZegoExpressEngine.getEngine().enableHardwareDecoder(true);
        ZegoExpressEngine.getEngine().setCapturePipelineScaleMode(ZegoCapturePipelineScaleMode.POST);
        ZegoExpressEngine.getEngine().setMinVideoBitrateForTrafficControl(120, ZegoTrafficControlMinVideoBitrateMode.ULTRA_LOW_FPS);
        ZegoExpressEngine.getEngine().setTrafficControlFocusOn(ZegoTrafficControlFocusOnMode.ZEGO_TRAFFIC_CONTROL_FOUNS_ON_REMOTE);
        ZegoExpressEngine.getEngine().enableANS(false);
        ZegoEngineConfig config = new ZegoEngineConfig();
        config.advancedConfig.put("room_retry_time", "30");
        ZegoExpressEngine.setEngineConfig(config);
    }

    @Override
    public ZegoAudioRoute getAudioRouteType() {
        return ZegoExpressEngine.getEngine().getAudioRouteType();
    }
}
