package im.zego.calluikit;

import androidx.annotation.NonNull;

import im.zego.callsdk.callback.ZegoTokenCallback;

/**
 * Created by rocket_wang on 2022/4/16.
 */
public interface ZegoCallTokenDelegate {
    void getToken(@NonNull String userID, @NonNull ZegoTokenCallback callback);
}