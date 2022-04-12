package im.zego.callsdk.core.interfaceimpl;

import android.util.Log;

import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.model.ZegoRoomInfo;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.core.interfaces.ZegoRoomService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.utils.CoreTest;
import im.zego.callsdk.utils.ZegoCallHelper;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class ZegoRoomServiceImpl extends ZegoRoomService {

    private static final String TAG = "ZegoRoomServiceImpl";

    /**
     * Join a room.
     * <p>
     * Description: This method can be used to join a room, the room must be an existing room.
     * <p>
     * Call this method at: After user logs in
     *
     * @param roomID refers to the ID of the room you want to join, and cannot be null.
     * @param token  refers to the authentication token. To get this, see the documentation:
     *               https://doc-en.zego.im/article/11648
     */
    public void joinRoom(String roomID, String token) {
        Log.d(CoreTest.TAG, "joinRoom() called with: roomID = [" + roomID + "], token = [" + token + "]");
        if (roomInfo == null) {
            roomInfo = new ZegoRoomInfo();
        }
        roomInfo.roomID = roomID;

        ZegoUserInfo localUserInfo = ZegoServiceManager.getInstance().userService.getLocalUserInfo();
        ZegoUser user = new ZegoUser(localUserInfo.userID, localUserInfo.userName);

        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.token = token;
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig);

        String streamID = ZegoCallHelper.getStreamID(localUserInfo.userID, roomID);
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);

        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        userService.userInfoList.clear();
        userService.userInfoList.add(localUserInfo);
    }

    /**
     * Leave the room
     * <p>
     * Description: This method can be used to leave the room you joined. The room will be ended when the Host leaves,
     * and all users in the room will be forced to leave the room.
     * <p>
     * Call this method at: After joining a room
     */
    public void leaveRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        userService.userInfoList.clear();
    }
}
