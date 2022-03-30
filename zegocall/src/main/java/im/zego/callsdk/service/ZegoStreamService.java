package im.zego.callsdk.service;

import android.view.TextureView;

public abstract class ZegoStreamService {

    /**
     * Start playing streams
     * <p>
     * Description: this can be used to play audio or video streams.
     * <p>
     * Call this method at: after joining a room
     *
     * @param userID     the ID of the user you are connecting
     * @param textureView refers to the view of local video preview
     */
    public abstract void startPlaying(String userID, TextureView textureView);

    /**
     * Stop playing streams
     * <p>
     * Description: this can be used to stop playing audio or video streams.
     * <p>
     * Call this method at: after joining a room
     *
     * @param userID the ID of the user you are connecting
     */
    public abstract void stopPlaying(String userID);
}