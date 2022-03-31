package im.zego.callsdk.service;

import android.util.Log;
import android.view.TextureView;

import im.zego.callsdk.utils.CoreTest;
import im.zego.callsdk.utils.ZegoCallHelper;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;

public class ZegoStreamServiceImpl extends ZegoStreamService{
    /**
     * Playback video streams data
     * <p>
     * Description: This can be used to intuitively play the video stream data, the audio stream data is played by
     * default. Call this method at: After joining a room
     *
     * @param userID      refers to the ID of the user you want to play the video streams from.
     * @param textureView refers to the target view that you want to be rendered.
     */
    public void startPlaying(String userID, TextureView textureView) {
        ZegoCanvas zegoCanvas = new ZegoCanvas(textureView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

        if (ZegoCallHelper.isUserIDSelf(userID)) {
            ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_0);
            ZegoExpressEngine.getEngine().startPreview(zegoCanvas);
        } else {
            String streamID = ZegoCallHelper.getStreamID(userID);
            Log.d(CoreTest.TAG, "startPlayingStream() called with: streamID = [" + streamID + "]");
            ZegoExpressEngine.getEngine().startPlayingStream(streamID, zegoCanvas);
        }
    }

    public void stopPlaying(String userID) {
        if (ZegoCallHelper.isUserIDSelf(userID)) {
            ZegoExpressEngine.getEngine().stopPreview();
        } else {
            String streamID = ZegoCallHelper.getStreamID(userID);
            ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        }
    }
}