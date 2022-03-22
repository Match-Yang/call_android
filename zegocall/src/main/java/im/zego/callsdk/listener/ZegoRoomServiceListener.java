package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoRoomInfo;

public interface ZegoRoomServiceListener {
    void onRoomInfoUpdated(ZegoRoomInfo roomInfo);
}
