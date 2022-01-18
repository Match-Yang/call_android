package im.zego.callsdk.model;

public enum ZegoResponseType {
    Accept(1),
    Decline(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoResponseType(int value) {
        this.value = value;
    }
}
