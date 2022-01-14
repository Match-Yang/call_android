package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ZegoUserService {

    public ZegoUserInfo localUserInfo;

    private List<ZegoUserInfo> userList;

    private ZegoUserServiceListener listener;

    public void login(ZegoUserInfo userInfo, String token, ZegoRoomCallback callback) {

    }

    public void logout() {

    }

    public void callToUser(String userID, ZegoCallType type) {

    }

    public void cancelCallToUser(String userID) {

    }

    public void responseCall(ZegoResponseType type) {

    }

    public void endCall() {

    }

    public void micOperate(boolean open, ZegoRoomCallback callback) {

    }

    public void cameraOperate(boolean open, ZegoRoomCallback callback) {

    }

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    public List<ZegoUserInfo> getUserList() {
        return userList;
    }

    void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {

    }

    void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {

    }

    void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {

    }

    void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {

    }
}
