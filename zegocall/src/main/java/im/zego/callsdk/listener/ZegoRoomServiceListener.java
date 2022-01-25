package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoRoomInfo;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.List;

public interface ZegoRoomServiceListener {
    // room info update
    void onReceiveRoomInfoUpdate(ZegoRoomInfo roomInfo);

    void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event);

    void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, List<ZegoStream> streamList);

}
