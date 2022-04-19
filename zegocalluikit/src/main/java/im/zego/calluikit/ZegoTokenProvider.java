package im.zego.calluikit;

import im.zego.callsdk.callback.ZegoTokenCallback;

public interface ZegoTokenProvider {
    void getToken(String userID, ZegoTokenCallback callback);
}