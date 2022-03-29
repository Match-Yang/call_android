package im.zego.callsdk.service;

import android.view.TextureView;

public abstract class ZegoStreamService {
    public abstract void startPlaying(String userID, TextureView textureView);

    public abstract void stopPlaying(String userID);
}