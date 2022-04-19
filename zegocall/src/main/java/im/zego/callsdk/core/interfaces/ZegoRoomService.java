package im.zego.callsdk.core.interfaces;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoRoomServiceListener;
import im.zego.callsdk.model.ZegoRoomInfo;

/**
 * Class LiveAudioRoom information management
 * <p>
 * Description: This class contains the room information management logic, such as the logic of create a room, join a
 * room, leave a room, disable the text chat in room, etc.
 */
public abstract class ZegoRoomService {

    public ZegoRoomInfo roomInfo = new ZegoRoomInfo();

    private ZegoRoomServiceListener listener;

    public abstract void joinRoom(String roomID, String token, ZegoCallback callback);

    public abstract void leaveRoom();

    public abstract void renewToken(String token,String roomID);

    public void setListener(ZegoRoomServiceListener listener) {
        this.listener = listener;
    }

    public ZegoRoomServiceListener getListener() {
        return listener;
    }
}
