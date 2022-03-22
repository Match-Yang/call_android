package im.zego.callsdk.callback;

public interface ZegoRequestCallback {

    void onResult(int errorCode, Object obj);
}
