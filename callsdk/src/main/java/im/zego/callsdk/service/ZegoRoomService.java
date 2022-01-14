package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoRoomServiceListener;
import im.zego.callsdk.model.ZegoRoomInfo;

public class ZegoRoomService {

    private ZegoRoomServiceListener listener;
    public ZegoRoomInfo roomInfo;

    public void createRoom(String roomID, String roomName, String token, ZegoRoomCallback callback) {

    }

    public void joinRoom(String roomID, String token, ZegoRoomCallback callback) {

    }

    public void leaveRoom(ZegoRoomCallback callback) {

    }

    public void setListener(ZegoRoomServiceListener listener) {
        this.listener = listener;
    }
}
