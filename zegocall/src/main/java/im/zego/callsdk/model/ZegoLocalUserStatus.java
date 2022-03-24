package im.zego.callsdk.model;

public enum ZegoLocalUserStatus {
    Free(1),
    Outgoing(2),
    Incoming(3),
    Calling(4);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoLocalUserStatus(int value) {
        this.value = value;
    }
}
