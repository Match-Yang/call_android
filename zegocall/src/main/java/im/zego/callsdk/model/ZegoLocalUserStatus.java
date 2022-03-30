package im.zego.callsdk.model;

public enum ZegoLocalUserStatus {
    // free: the state of user is free
    Free(1),
    // outgoing: the user is making a outbound call
    Outgoing(2),
    // incoming: the user is receiving a call
    Incoming(3),
    // calling: the user is already on a call
    Calling(4);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoLocalUserStatus(int value) {
        this.value = value;
    }
}
