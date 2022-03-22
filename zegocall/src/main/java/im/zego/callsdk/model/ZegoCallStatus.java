package im.zego.callsdk.model;

public enum ZegoCallStatus {
    Free(1),
    Outgoing(2),
    Incoming(3),
    Calling(3);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCallStatus(int value) {
        this.value = value;
    }
}
