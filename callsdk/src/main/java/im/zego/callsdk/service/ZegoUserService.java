package im.zego.callsdk.service;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.zego.callsdk.ZegoZIMManager;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallMessage;
import im.zego.callsdk.model.ZegoCallMessage.UserInfoBean;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CustomTypeAdapterFactory;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

public class ZegoUserService {

    public ZegoUserInfo localUserInfo;

    private List<ZegoUserInfo> userList;

    private static final String TAG = "UserService";

    private ZegoUserServiceListener listener;
    private ZegoRoomService roomService;
    private static Gson mGson;

    public ZegoUserService() {
        roomService = new ZegoRoomService();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new CustomTypeAdapterFactory());
        mGson = builder.create();
        userList = new ArrayList<>();
    }

    public void login(ZegoUserInfo userInfo, String token, ZegoRoomCallback callback) {
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userInfo.userID;
        zimUserInfo.userName = userInfo.userName;
        ZegoZIMManager.getInstance().zim.login(zimUserInfo, token, new ZIMLoggedInCallback() {
            @Override
            public void onLoggedIn(ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    localUserInfo = new ZegoUserInfo();
                    localUserInfo.userID = userInfo.userID;
                    localUserInfo.userName = userInfo.userName;
                }
                if (callback != null) {
                    callback.onRoomCallback(errorInfo.code.value());
                }
            }
        });
    }

    // user logout
    public void logout() {
        Log.d(TAG, "logout() called");
        ZegoZIMManager.getInstance().zim.logout();
        leaveRoom();
    }

    void leaveRoom() {
        userList.clear();
    }

    public void callToUser(String userID, ZegoCallType callType, String token, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            String roomID = localUserInfo.userID;
            roomService.createRoom(roomID, localUserInfo.userName, token, errorCode -> {
                Log.d(TAG, "createRoom: " + errorCode + ",roomID:" + roomID);
                if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                    ZegoCallMessage callMessage = new ZegoCallMessage();
                    callMessage.actionType = ZegoCallMessage.CALL;
                    callMessage.target = Collections.singletonList(userID);
                    callMessage.content.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
                    callMessage.content.callType = callType;
                    String messageJson = mGson.toJson(callMessage);
                    ZIMCustomMessage custom = new ZIMCustomMessage();
                    custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
                    ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
                        if (callback != null) {
                            callback.onRoomCallback(errorInfo.code.value());
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onRoomCallback(errorCode);
                    }
                }
            });
        } else {
            callback.onRoomCallback(ZIMErrorCode.NO_LOGIN.value());
        }
    }

    public void cancelCallToUser(String userID, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            ZegoCallMessage callMessage = new ZegoCallMessage();
            callMessage.actionType = ZegoCallMessage.CANCEL_CALL;
            callMessage.target = Collections.singletonList(userID);
            callMessage.content.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
            String messageJson = mGson.toJson(callMessage);
            ZIMCustomMessage custom = new ZIMCustomMessage();
            custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
            ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
                if (callback != null) {
                    callback.onRoomCallback(errorInfo.code.value());
                }
            });
        } else {
            if (callback != null) {
                callback.onRoomCallback(ZIMErrorCode.NO_LOGIN.value());
            }
        }
    }

    public void responseCall(ZegoResponseType type, String userID, String token, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            if (type == ZegoResponseType.Accept) {
                roomService.joinRoom(userID, token, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        responseCallInner(type, userID, errorCode1 -> {
                            if (errorCode1 == ZIMErrorCode.SUCCESS.value()) {
                                ZegoExpressEngine.getEngine().startPublishingStream(getSelfStreamID());
                                ZegoExpressEngine.getEngine().enableCamera(true);
                                ZegoExpressEngine.getEngine().muteMicrophone(false);
                            }
                            if (callback != null) {
                                callback.onRoomCallback(errorCode1);
                            }
                        });
                    } else {
                        if (callback != null) {
                            callback.onRoomCallback(errorCode);
                        }
                    }
                });
            } else {
                responseCallInner(type, userID, callback);
            }
        } else {
            if (callback != null) {
                callback.onRoomCallback(ZIMErrorCode.NO_LOGIN.value());
            }
        }
    }

    private void responseCallInner(ZegoResponseType type, String userID, ZegoRoomCallback callback) {
        ZegoCallMessage callMessage = new ZegoCallMessage();
        callMessage.actionType = ZegoCallMessage.RESPONSE_CALL;
        callMessage.target = Collections.singletonList(userID);
        callMessage.content.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
        callMessage.content.responseType = type;
        String messageJson = mGson.toJson(callMessage);
        ZIMCustomMessage custom = new ZIMCustomMessage();
        custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
        ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
            if (callback != null) {
                callback.onRoomCallback(errorInfo.code.value());
            }
        });
    }

    public void endCall(ZegoRoomCallback callback) {
        roomService.leaveRoom(callback);
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

    private String getSelfStreamID() {
        String selfUserID = localUserInfo.userID;
        String roomID = roomService.roomInfo.roomID;
        return String.format("%s_%s_%s", roomID, selfUserID, "main");
    }
}
