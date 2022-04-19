package im.zego.callsdk.listener;

public interface ZegoRoomServiceListener {

    void onRoomTokenWillExpire(int timeInSeconds, String roomID);
}
