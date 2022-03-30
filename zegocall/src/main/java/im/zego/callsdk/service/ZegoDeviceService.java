package im.zego.callsdk.service;

import im.zego.callsdk.listener.ZegoDeviceServiceListener;
import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.zegoexpress.constants.ZegoAudioRoute;

/**
 * Class device management
 * <p>
 * Description: This class contains the device settings related logic for you to configure different device settings.
 */
public abstract class ZegoDeviceService {

    // The listener instance of the device service.
    public ZegoDeviceServiceListener listener;

    private ZegoVideoResolution videoResolution;
    private ZegoAudioBitrate bitrate;
    private boolean noiseSliming;
    private boolean echoCancellation;
    private boolean volumeAdjustment;

    public void setListener(ZegoDeviceServiceListener listener) {
        this.listener = listener;
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
    public abstract void setDeviceStatus(ZegoDevicesType devicesType, boolean enable);

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
    public abstract void setVideoResolution(ZegoVideoResolution videoResolution);

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
    public abstract void setAudioBitrate(ZegoAudioBitrate audioBitrate);

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
    public abstract void enableCamera(boolean enable);

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
    public abstract void muteMic(boolean mute);

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
    public abstract void useFrontCamera(boolean isFront);

    /**
     * Use speaker or receiver
     * <p>
     * Description: This can be used to set the speaker and receiver.
     * <p>
     * Call this method at: After joining a room
     *
     * @param enable determines whether to use the speaker or the receiver. true: use the speaker. false: use the receiver.
     */
    public abstract void enableSpeaker(boolean enable);

    public abstract void setBestConfig();

    public abstract ZegoAudioRoute getAudioRouteType();

}
