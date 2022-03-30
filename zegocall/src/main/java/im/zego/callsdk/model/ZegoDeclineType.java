package im.zego.callsdk.model;

public enum ZegoDeclineType {
    /// decline: the call was declined by the callee
    Decline(1),
    /// busy: the call was timed out because the callee is busy
    Busy(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoDeclineType(int value) {
        this.value = value;
    }
}
