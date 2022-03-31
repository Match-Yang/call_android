package im.zego.callsdk.model;

public enum ZegoCallTimeoutType {
    Connecting(1),
    Calling(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCallTimeoutType(int value) {
        this.value = value;
    }
}
