package im.zego.callsdk.service;

import android.util.Log;
import android.view.TextureView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import im.zego.callsdk.ZegoZIMManager;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallMessage;
import im.zego.callsdk.model.ZegoCallMessage.ContentBean;
import im.zego.callsdk.model.ZegoCallMessage.UserInfoBean;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoRoomInfo;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CustomTypeAdapterFactory;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.json.JSONObject;

public class ZegoUserService {

    public ZegoUserInfo localUserInfo;

    private List<ZegoUserInfo> userList;

    private static final String TAG = "UserService";

    private ZegoUserServiceListener listener;
    private ZegoRoomService roomService;
    private static Gson mGson;
    private Map<String, String> streamMap = new HashMap();

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

    public void callUser(String userID, ZegoCallType callType, String createRoomToken, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            String roomID = localUserInfo.userID;
            roomService.createRoom(roomID, localUserInfo.userName, createRoomToken, errorCode -> {
                Log.d(TAG, "createRoom: " + errorCode + ",roomID:" + roomID);
                if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                    ZegoCallMessage callMessage = new ZegoCallMessage();
                    callMessage.actionType = ZegoCallMessage.CALL;
                    callMessage.target = Collections.singletonList(userID);
                    ZegoCallMessage.ContentBean contentBean = new ContentBean();
                    contentBean.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
                    contentBean.callType = callType;
                    callMessage.content = contentBean;
                    String messageJson = mGson.toJson(callMessage);
                    ZIMCustomMessage custom = new ZIMCustomMessage();
                    custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
                    ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
                        if (callback != null) {
                            callback.onRoomCallback(errorInfo.code.value());
                        }
                    });
                    ZegoExpressEngine.getEngine().startPublishingStream(getStreamIDFromUser(localUserInfo.userID));
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

    public void cancelCall(ZegoCancelType cancelType, String userID, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            ZegoCallMessage callMessage = new ZegoCallMessage();
            callMessage.actionType = ZegoCallMessage.CANCEL_CALL;
            callMessage.target = Collections.singletonList(userID);
            ZegoCallMessage.ContentBean contentBean = new ContentBean();
            contentBean.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
            contentBean.cancelType = cancelType;
            callMessage.content = contentBean;
            String messageJson = mGson.toJson(callMessage);
            ZIMCustomMessage custom = new ZIMCustomMessage();
            custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
            ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
                if (callback != null) {
                    callback.onRoomCallback(errorInfo.code.value());
                }
            });
            roomService.leaveRoom(errorCode -> {

            });
        } else {
            if (callback != null) {
                callback.onRoomCallback(ZIMErrorCode.NO_LOGIN.value());
            }
        }
    }

    public void respondCall(ZegoResponseType type, String userID, String joinRoomToken, ZegoRoomCallback callback) {
        if (localUserInfo != null) {
            if (type == ZegoResponseType.Accept) {
                roomService.joinRoom(userID, joinRoomToken, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        responseCallInner(type, userID, errorCode1 -> {
                            if (errorCode1 == ZIMErrorCode.SUCCESS.value()) {
                                ZegoExpressEngine.getEngine()
                                    .startPublishingStream(getStreamIDFromUser(localUserInfo.userID));
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
        ZegoCallMessage.ContentBean contentBean = new ContentBean();
        contentBean.userInfo = new UserInfoBean(localUserInfo.userID, localUserInfo.userName);
        contentBean.responseType = type;
        callMessage.content = contentBean;
        String messageJson = mGson.toJson(callMessage);
        ZIMCustomMessage custom = new ZIMCustomMessage();
        custom.message = messageJson.getBytes(StandardCharsets.UTF_8);
        ZegoZIMManager.getInstance().zim.sendPeerMessage(custom, userID, (message, errorInfo) -> {
            Log.d(TAG, "responseCallInner() called with: message = [" + message + "], errorInfo = [" + errorInfo.message
                + "]");
            if (callback != null) {
                callback.onRoomCallback(errorInfo.code.value());
            }
        });
    }

    public void endCall(ZegoRoomCallback callback) {
        Log.d(TAG, "endCall() called with: callback = [" + callback + "]");
        roomService.leaveRoom(errorCode -> {
            if (callback != null) {
                callback.onRoomCallback(errorCode);
            }
        });
    }

    public void enableMic(boolean enable, ZegoRoomCallback callback) {
        boolean micState = localUserInfo.mic;
        if (micState == enable) {
            callback.onRoomCallback(0);
            return;
        }
        localUserInfo.mic = enable;
        HashMap<String, String> seatAttributes = new HashMap<>();
        seatAttributes.put(localUserInfo.userID, mGson.toJson(localUserInfo));

        String roomID = roomService.roomInfo.roomID;
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;

        Log.d(TAG, "micOperate() called with: seatAttributes = [" + seatAttributes + "],roomID:" + roomID);

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
            Log.d(TAG, "micOperate: errorInfo " + errorInfo.message + ",localUserInfo:" + localUserInfo);
            if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                if (listener != null) {
                    listener.onUserInfoUpdated(localUserInfo);
                }
            } else {
                localUserInfo.mic = micState;
            }
            ZegoExpressEngine.getEngine().muteMicrophone(!localUserInfo.mic);
            callback.onRoomCallback(errorInfo.code.value());
        });
    }

    public void enableCamera(boolean open, ZegoRoomCallback callback) {
        boolean cameraState = localUserInfo.camera;
        if (cameraState == open) {
            callback.onRoomCallback(0);
            return;
        }
        localUserInfo.camera = open;
        HashMap<String, String> seatAttributes = new HashMap<>();
        seatAttributes.put(localUserInfo.userID, mGson.toJson(localUserInfo));

        String roomID = roomService.roomInfo.roomID;
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;

        Log.d(TAG, "cameraOperate() called with: seatAttributes = [" + seatAttributes + "],roomID:" + roomID);

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
            Log.d(TAG, "cameraOperate: errorInfo " + errorInfo.message + ",localUserInfo:" + localUserInfo);
            if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                if (listener != null) {
                    listener.onUserInfoUpdated(localUserInfo);
                }
            } else {
                localUserInfo.camera = cameraState;
            }
            ZegoExpressEngine.getEngine().enableCamera(localUserInfo.camera);
            callback.onRoomCallback(errorInfo.code.value());
        });
    }

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    public List<ZegoUserInfo> getUserList() {
        return userList;
    }

    void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {
        if (listener != null) {
            listener.onConnectionStateChanged(state, event);
        }
    }

    void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZIMCustomMessage customMessage = (ZIMCustomMessage) zimMessage;
                String messageJson = new String(customMessage.message);
                ZegoCallMessage callMessage = mGson.fromJson(messageJson, ZegoCallMessage.class);
                if (callMessage.target.contains(localUserInfo.userID)) {
                    ZegoUserInfo userInfo = new ZegoUserInfo();
                    userInfo.userID = callMessage.content.userInfo.userID;
                    userInfo.userName = callMessage.content.userInfo.userName;
                    if (callMessage.actionType == ZegoCallMessage.CALL) {
                        // when received call ,we assume that user mic and camera is open
                        ZegoCallType callType = callMessage.content.callType;
                        if (callType == ZegoCallType.Video) {
                            userInfo.camera = true;
                        }
                        userInfo.mic = true;
                        if (listener != null) {
                            listener.onReceiveCallInvite(userInfo, callType);
                        }
                    } else if (callMessage.actionType == ZegoCallMessage.CANCEL_CALL) {
                        if (listener != null) {
                            listener.onReceiveCallCanceled(userInfo, callMessage.content.cancelType);
                        }
                    } else if (callMessage.actionType == ZegoCallMessage.RESPONSE_CALL) {
                        if (listener != null) {
                            listener.onReceiveCallResponse(userInfo, callMessage.content.responseType);
                        }
                    }
                }
            }
        }

    }

    void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> joinUsers = generateRoomUsers(memberList);
        Iterator<ZegoUserInfo> iterator = joinUsers.iterator();
        while (iterator.hasNext()) {
            ZegoUserInfo next = iterator.next();
            Log.d(TAG, "onRoomMemberJoined() called with: joinUser = [" + next);
            if (!userList.contains(next)) {
                userList.add(next); // avoid duplicate
            } else {
                // if duplicate,don't notify outside
                iterator.remove();
            }
        }
    }

    void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> leaveUsers = generateRoomUsers(memberList);
        for (ZegoUserInfo leaveUser : leaveUsers) {
            stopPlaying(leaveUser.userID);
            userList.remove(leaveUser);
        }
        Log.d(TAG, "onRoomMemberLeft: " + leaveUsers);
        if (userList.size() <= 1 && listener != null) {
            // only self left
            listener.onReceiveCallEnded();
        }
    }

    private List<ZegoUserInfo> generateRoomUsers(List<ZIMUserInfo> memberList) {

        List<ZegoUserInfo> roomUsers = new ArrayList<>();
        for (ZIMUserInfo userInfo : memberList) {
            ZegoUserInfo roomUser = new ZegoUserInfo();
            roomUser.userID = (userInfo.userID);
            roomUser.userName = (userInfo.userName);
            roomUsers.add(roomUser);
        }
        return roomUsers;
    }

    private String getStreamIDFromUser(String userID) {
        String roomID = roomService.roomInfo.roomID;
        return String.format("%s_%s_%s", roomID, userID, "main");
    }

    void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
        HashMap<String, String> roomAttributes = info.roomAttributes;
        Log.d(TAG,
            "onRoomAttributesUpdated() called with: zim = [" + zim + "], roomAttributes = [" + roomAttributes + "]");
        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
            for (Entry<String, String> entry : roomAttributes.entrySet()) {
                Log.d(TAG, "onRoomAttributesUpdated,entry : " + entry);
                String key = entry.getKey();
                String value = entry.getValue();
                if (Objects.equals(key, ZegoRoomService.KEY_ROOM_INFO)) {
                    ZegoRoomInfo roomInfo = mGson.fromJson(roomAttributes.get(key), ZegoRoomInfo.class);
                    roomService.updateRoomInfo(roomInfo);
                    if (roomInfo == null && listener != null) {
                        listener.onReceiveCallEnded();
                    }
                } else {
                    ZegoUserInfo attrUserInfo = mGson.fromJson(value, ZegoUserInfo.class);
                    for (ZegoUserInfo userInfo : userList) {
                        if (Objects.equals(userInfo.userID, localUserInfo.userID)) {
                            // skip self
                            continue;
                        }
                        if (Objects.equals(userInfo.userID, attrUserInfo.userID)) {
                            // update user state
                            final boolean nameChanged = Objects.equals(userInfo.userName, attrUserInfo.userName);
                            final boolean micChanged = Objects.equals(userInfo.mic, attrUserInfo.mic);
                            final boolean cameraChanged = Objects.equals(userInfo.camera, attrUserInfo.camera);
                            userInfo.userName = attrUserInfo.userName;
                            userInfo.mic = attrUserInfo.mic;
                            userInfo.camera = attrUserInfo.camera;
                            if (nameChanged || micChanged || cameraChanged) {
                                if (listener != null) {
                                    listener.onUserInfoUpdated(userInfo);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            if (listener != null) {
                listener.onReceiveCallEnded();
            }
        }

    }

    public void speakerOperate(boolean open) {
        ZegoExpressEngine.getEngine().muteSpeaker(!open);
    }

    public void startPlaying(String userID, TextureView textureView) {
        ZegoCanvas zegoCanvas = new ZegoCanvas(textureView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

        if (Objects.equals(localUserInfo.userID, userID)) {
            ZegoExpressEngine.getEngine().setAppOrientation(ZegoOrientation.ORIENTATION_0);
            ZegoExpressEngine.getEngine().startPreview(zegoCanvas);
        } else {
            ZegoExpressEngine.getEngine().startPlayingStream(getStreamIDFromUser(userID), zegoCanvas);
        }
    }

    public void stopPlaying(String userID) {
        if (Objects.equals(localUserInfo.userID, userID)) {
            ZegoExpressEngine.getEngine().stopPreview();
        } else {
            String streamID = streamMap.get(userID);
            ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        }
    }

    public void useFrontCamera(boolean enable) {
        ZegoExpressEngine.getEngine().useFrontCamera(enable);
    }

    public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
        String roomID) {
        if (state == ZIMRoomState.DISCONNECTED) {
            roomService.leaveRoom(null);
            if (listener != null) {
                listener.onReceiveCallEnded();
            }
        }
    }
}
