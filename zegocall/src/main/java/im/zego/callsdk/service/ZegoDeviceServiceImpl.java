package im.zego.callsdk.service;


import android.view.TextureView;
import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
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

    }

    /**
     * Playback video streams data
     * <p>
     * Description: This can be used to intuitively play the video stream data, the audio stream data is played by
     * default. Call this method at: After joining a room
     *
     * @param userID      refers to the ID of the user you want to play the video streams from.
     * @param textureView refers to the target view that you want to be rendered.
     */
    public void playVideoStream(String userID, TextureView textureView) {
        ZegoCanvas zegoCanvas = new ZegoCanvas(textureView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

//        if (UserInfoHelper.isUserIDSelf(userID)) {
//            ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_0);
//            ZegoExpressEngine.getEngine().startPreview(zegoCanvas);
//        } else {
//            String streamID = ZegoLiveHelper.getStreamID(userID);
//            ZegoExpressEngine.getEngine().startPlayingStream(streamID, zegoCanvas);
//        }
    }

    public void stopPlayStream(String userID) {
//        if (UserInfoHelper.isUserIDSelf(userID)) {
//            ZegoExpressEngine.getEngine().stopPreview();
//        } else {
//            String streamID = ZegoLiveHelper.getStreamID(userID);
//            ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
//        }
    }
}
