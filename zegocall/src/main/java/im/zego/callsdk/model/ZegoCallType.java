package im.zego.callsdk.model;

public enum ZegoCallType {
    /// voice: voice call
    Voice(1),
    /// video: video call
    Video(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCallType(int value) {
        this.value = value;
    }
}
