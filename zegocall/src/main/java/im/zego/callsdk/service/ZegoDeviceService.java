package im.zego.callsdk.service;


import android.text.TextUtils;
import android.view.TextureView;

import java.util.Objects;

import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;

/**
 * Class device management
 * <p>
 * Description: This class contains the device settings related logic for you to configure different device settings.
 */
public class ZegoDeviceService {

    /**
     * Camera related operations
     * <p>
     * Description: This method can be enable or disable the camera.
     * The video streams will be automatically published to remote users when the camera is on.
     * <p>
     * Call this method at: After joining a room
     *
     * @param enable determines whether to enable or disable the camera. true: Enable false: Disable
     */
    void enableCamera(boolean enable) {
        ZegoExpressEngine.getEngine().enableCamera(enable);
    }

    /**
     * Microphone related operations
     * <p>
     * Description: This method can be used to mute or unmute the microphone.
     * The audio streams will be automatically published to remote users when the microphone is on.
     * <p>
     * Call this method at: After joining a room
     *
     * @param mute determines whether to mute or unmute the microphone. true: Mute false: Unmute
     */
    void muteMic(boolean mute) {
        ZegoExpressEngine.getEngine().muteMicrophone(mute);
    }

    /**
     * Use front-facing and rear camera
     * <p>
     * Description: This method can be used to set the camera, the SDK uses the front-facing camera by default.
     * <p>
     * Call this method at: After joining a room
     *
     * @param isFront determines whether to use the front-facing camera or the rear camera.
     *                true: Use front-facing camera. false: Use rear camera.
     */
    public void useFrontCamera(boolean isFront) {
        ZegoExpressEngine.getEngine().useFrontCamera(isFront);
    }

    /**
     * Playback video streams data
     * <p>
     * Description: This can be used to intuitively play the video stream data, the audio stream data is played by default.
     * Call this method at: After joining a room
     *
     * @param userID      refers to the ID of the user you want to play the video streams from.
     * @param textureView refers to the target view that you want to be rendered.
     */
    public void startPlayStream(String userID, TextureView textureView) {
        ZegoCanvas zegoCanvas = new ZegoCanvas(textureView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

        if (isUserIDSelf(userID)) {
            ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_0);
            ZegoExpressEngine.getEngine().startPreview(zegoCanvas);
        } else {
            String streamID = getStreamID(userID);
            ZegoExpressEngine.getEngine().startPlayingStream(streamID, zegoCanvas);
        }
    }

    public void stopPlayStream(String userID) {
        if (isUserIDSelf(userID)) {
            ZegoExpressEngine.getEngine().stopPreview();
        } else {
            String streamID = getStreamID(userID);
            ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        }
    }

    private boolean isUserIDSelf(String userID) {
        ZegoUserInfo selfUser = ZegoRoomManager.getInstance().userService.localUserInfo;
        return Objects.equals(selfUser.userID, userID) && !TextUtils.isEmpty(userID);
    }

    private String getStreamID(String userID) {
        return ZegoRoomManager.getInstance().userService.getStreamIDFromUser(userID);
    }
}
