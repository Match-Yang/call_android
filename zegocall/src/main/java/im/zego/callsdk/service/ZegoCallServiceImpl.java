package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoAcceptCallCommand;
import im.zego.callsdk.command.ZegoCallCommand;
import im.zego.callsdk.command.ZegoCancelCallCommand;
import im.zego.callsdk.command.ZegoEndCallCommand;
import im.zego.callsdk.command.ZegoDeclineCallCommand;
import im.zego.callsdk.listener.ZegoListener;
import im.zego.callsdk.model.ZegoCallInfo;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoDeclineType;
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
            ZegoCancelCallCommand command = new ZegoCancelCallCommand();
            command.execute(new ZegoRequestCallback() {
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
    public void acceptCall(String joinToken, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        if (userService.localUserInfo != null) {
            ZegoAcceptCallCommand command = new ZegoAcceptCallCommand();
            command.execute(new ZegoRequestCallback() {
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
    public void declineCall(String userID, ZegoDeclineType type, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            ZegoDeclineCallCommand command = new ZegoDeclineCallCommand();
            command.execute(new ZegoRequestCallback() {
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
    public void endCall(ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoEndCallCommand command = new ZegoEndCallCommand();
            command.fromUserID = selfUserID;
            command.execute(new ZegoRequestCallback() {
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
    public void setCallInfo(ZegoCallInfo callInfo) {
        super.setCallInfo(callInfo);
    }
}
