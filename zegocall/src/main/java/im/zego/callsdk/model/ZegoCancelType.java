package im.zego.callsdk.model;

public enum ZegoCancelType {
    /// intent: the caller canceled the call invitation
    INTENT(1),
    TIMEOUT(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoCancelType(int value) {
        this.value = value;
    }
}
