package im.zego.callsdk.service;

import android.util.Log;
import com.google.gson.Gson;
import im.zego.callsdk.ZegoZIMManager;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.model.ZegoRoomInfo;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.HashMap;

public class ZegoRoomServiceImpl extends ZegoRoomService {

    private static final String TAG = "ZegoRoomServiceImpl";

    /**
     * Create a room
     * <p>
     * Description: This method can be used to create a room. The room creator will be the Host by default when the room
     * is created successfully.
     * <p>
     * Call this method at: After user logs in
     *
     * @param roomID   refers to the room ID, the unique identifier of the room. This is required to join a room and
     *                 cannot be null.
     * @param roomName refers to the room name. This is used for display in the room and cannot be null.
     * @param token    refers to the authentication token. To get this, see the documentation:
     *                 https://doc-en.zego.im/article/11648
     * @param callback refers to the callback for create a room.
     */
    public void createRoom(String roomID, String roomName, String token, ZegoCallback callback) {
        ZegoUserInfo localUserInfo = ZegoServiceManager.getInstance().userService.localUserInfo;

        roomInfo.roomID = (roomID);
        roomInfo.roomName = (roomName);

        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        zimRoomInfo.roomName = roomName;

        Gson gson = ZegoServiceManager.getInstance().mGson;
        HashMap<String, String> roomAttributes = new HashMap<>();
        roomAttributes.put(KEY_ROOM_INFO, gson.toJson(roomInfo));
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();
        config.roomAttributes = roomAttributes;

        ZegoZIMManager.getInstance().zim.createRoom(zimRoomInfo, config, (roomInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                loginRTCRoom(roomID, token, localUserInfo);
            }
            if (callback != null) {
                callback.onResult(errorInfo.code.value());
            }
        });
    }

    /**
     * Join a room.
     * <p>
     * Description: This method can be used to join a room, the room must be an existing room.
     * <p>
     * Call this method at: After user logs in
     *
     * @param roomID   refers to the ID of the room you want to join, and cannot be null.
     * @param token    refers to the authentication token. To get this, see the documentation:
     *                 https://doc-en.zego.im/article/11648
     * @param callback refers to the callback for join a room.
     */
    public void joinRoom(String roomID, String token, ZegoCallback callback) {
        ZegoUserInfo localUserInfo = ZegoServiceManager.getInstance().userService.localUserInfo;

        ZegoZIMManager.getInstance().zim.joinRoom(roomID, (roomInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                loginRTCRoom(roomID, token, localUserInfo);
                this.roomInfo.roomID = (roomInfo.baseInfo.roomID);
                this.roomInfo.roomName = (roomInfo.baseInfo.roomName);
            }
            if (callback != null) {
                callback.onResult(errorInfo.code.value());
            }
        });
    }

    /**
     * Leave the room
     * <p>
     * Description: This method can be used to leave the room you joined. The room will be ended when the Host leaves,
     * and all users in the room will be forced to leave the room.
     * <p>
     * Call this method at: After joining a room
     *
     * @param callback refers to the callback for leave a room.
     */
    public void leaveRoom(ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        //        if (userService != null) {
        //            userService.leaveRoom();
        //        }

        ZegoExpressEngine.getEngine().stopSoundLevelMonitor();
        ZegoExpressEngine.getEngine().stopPublishingStream();

        ZegoExpressEngine.getEngine().logoutRoom(roomInfo.roomID);

        ZegoZIMManager.getInstance().zim.leaveRoom(roomInfo.roomID, errorInfo -> {
            Log.d(TAG, "leaveRoom() called with: errorInfo = [" + errorInfo.code + "]" + errorInfo.message);
            if (callback != null) {
                callback.onResult(errorInfo.code.value());
            }
        });
    }

    private void loginRTCRoom(String roomID, String token, ZegoUserInfo localUserInfo) {
        ZegoUser user = new ZegoUser(localUserInfo.userID, localUserInfo.userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.token = token;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig);
        ZegoExpressEngine.getEngine().startSoundLevelMonitor(500);
    }

    public void updateRoomInfo(ZegoRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }
}
