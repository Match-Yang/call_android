package im.zego.callsdk.callback;

import androidx.annotation.Nullable;

public interface ZegoTokenCallback {
    void onTokenCallback(int errorCode, @Nullable String token);
}
