package im.zego.callsdk.service;


import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

/**
 * Class device management
 * <p>
 * Description: This class contains the device settings related logic for you to configure different device settings.
 */
public class ZegoDeviceServiceImpl extends ZegoDeviceService {

    /**
     * Set video resolution
     * <p>
     * Description: This method can be used to set video resolution. A larger resolution consumes more network
     * bandwidth. You can select the resolution based on service requirements and network conditions. The default value
     * is 720P.
     * <p>
     * Call this method at: After joining a room
     *
     * @param videoResolution refers to the resolution value.
     */
    public void setVideoResolution(ZegoVideoResolution videoResolution) {
        ZegoVideoConfigPreset configPreset = ZegoVideoConfigPreset.getZegoVideoConfigPreset(videoResolution.value());
        ZegoVideoConfig videoConfig = new ZegoVideoConfig(configPreset);
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
    }

    /**
     * Set audio bitrate
     * <p>
     * Description: This method can be used to set audio bitrate. A larger audio bitrate consumes more network
     * bandwidth. You can select the bitrate based on service requirements and network conditions. The default value is
     * 48kbps.
     * <p>
     * Call this method at: After joining a room
     *
     * @param audioBitrate refers to the bitrate value.
     */
    public void setAudioBitrate(ZegoAudioBitrate audioBitrate) {
        ZegoAudioConfig audioConfig = new ZegoAudioConfig();
        audioConfig.bitrate = audioBitrate.value();
        ZegoExpressEngine.getEngine().setAudioConfig(audioConfig);
    }

    /**
     * Configure device settings
     * <p>
     * Description: This method can be used to configure device settings as actual business requirements.
     * <p>
     * Call this method at: After joining a room
     *
     * @param devicesType refers to the configuration type.
     * @param enable      determines whether to enable or disable.
     */
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

    /**
     * Camera related operations
     * <p>
     * Description: This method can be enable or disable the camera. The video streams will be automatically published
     * to remote users when the camera is on.
     * <p>
     * Call this method at: After joining a room
     *
     * @param enable determines whether to enable or disable the camera. true: Enable false: Disable
     */
    public void enableCamera(boolean enable) {
        ZegoExpressEngine.getEngine().enableCamera(enable);
    }

    /**
     * Microphone related operations
     * <p>
     * Description: This method can be used to mute or unmute the microphone. The audio streams will be automatically
     * published to remote users when the microphone is on.
     * <p>
     * Call this method at: After joining a room
     *
     * @param mute determines whether to mute or unmute the microphone. true: Mute false: Unmute
     */
    public void muteMic(boolean mute) {
        ZegoExpressEngine.getEngine().muteMicrophone(mute);
    }

    /**
     * Use front-facing and rear camera
     * <p>
     * Description: This method can be used to set the camera, the SDK uses the front-facing camera by default.
     * <p>
     * Call this method at: After joining a room
     *
     * @param isFront determines whether to use the front-facing camera or the rear camera. true: Use front-facing
     *                camera. false: Use rear camera.
     */
    public void useFrontCamera(boolean isFront) {
        ZegoExpressEngine.getEngine().useFrontCamera(isFront);
    }

    @Override
    public void enableSpeaker(boolean enable) {
        ZegoExpressEngine.getEngine().setAudioRouteToSpeaker(enable);
    }

    @Override
    public ZegoAudioRoute getAudioRouteType() {
        return ZegoExpressEngine.getEngine().getAudioRouteType();
    }
}
