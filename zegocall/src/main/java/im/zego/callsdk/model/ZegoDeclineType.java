package im.zego.callsdk.model;

public enum ZegoDeclineType {
    Decline(1),
    Busy(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoDeclineType(int value) {
        this.value = value;
    }
}
