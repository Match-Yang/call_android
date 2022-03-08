package im.zego.callsdk.model;

public enum ZegoCallType {
    Voice(1),
    Video(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCallType(int value) {
        this.value = value;
    }
}
