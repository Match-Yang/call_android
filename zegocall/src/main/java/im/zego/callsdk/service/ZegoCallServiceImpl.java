package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoCallCommand;
import im.zego.callsdk.command.ZegoCancelCallCommand;
import im.zego.callsdk.command.ZegoEndCallCommand;
import im.zego.callsdk.command.ZegoRespondCallCommand;
import im.zego.callsdk.listener.ZegoListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZegoCallServiceImpl extends ZegoCallService {

    private static final String TAG = "CallServiceImpl";
    private ZegoListener listener;

    @Override
    public void callUser(String userID, ZegoCallType callType, String createRoomToken, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            String callID = selfUserID + System.currentTimeMillis();
            List<String> target = Collections.singletonList(userID);
            ZegoCallCommand callCommand = new ZegoCallCommand();
            callCommand.putParameter("userID", selfUserID);
            callCommand.putParameter("callID", callID);
            callCommand.putParameter("callees", target);
            callCommand.putParameter("callType", callType);
            callCommand.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (callback != null) {
                        callback.onResult(errorCode);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void cancelCall(String userID, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoCancelCallCommand command = new ZegoCancelCallCommand();
            command.fromUserID = selfUserID;
            command.toUserID = userID;
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {

                }
            });
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void respondCall(String userID, String joinRoomToken, ZegoResponseType type, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoRespondCallCommand command = new ZegoRespondCallCommand();
            command.fromUserID = selfUserID;
            command.toUserID = userID;
            command.responseType = type;
            command.token = joinRoomToken;
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {

                }
            });
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void endCall(ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoEndCallCommand command = new ZegoEndCallCommand();
            command.fromUserID = selfUserID;
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {

                }
            });
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }
}
