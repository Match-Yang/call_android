package im.zego.callsdk.model;

public enum ZegoCallTimeoutType {
    Inviter(1),
    Invitee(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCallTimeoutType(int value) {
        this.value = value;
    }
}
