package im.zego.callsdk.service;

import im.zego.callsdk.model.ZegoRoomInfo;

/**
 * Class LiveAudioRoom information management
 * <p>
 * Description: This class contains the room information management logic,
 * such as the logic of create a room, join a room, leave a room, disable the text chat in room, etc.
 */
public abstract class ZegoRoomService {

    public ZegoRoomInfo roomInfo = new ZegoRoomInfo();

    public static final String KEY_ROOM_INFO = "room_info";
    private static final String TAG = "RoomService";

}
