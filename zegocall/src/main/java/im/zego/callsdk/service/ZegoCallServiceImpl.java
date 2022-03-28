package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoNotifyListener;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoAcceptCallCommand;
import im.zego.callsdk.command.ZegoCallCommand;
import im.zego.callsdk.command.ZegoCancelCallCommand;
import im.zego.callsdk.command.ZegoDeclineCallCommand;
import im.zego.callsdk.command.ZegoEndCallCommand;
import im.zego.callsdk.command.ZegoListenCallCommand;
import im.zego.callsdk.model.ZegoCallInfo;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ZegoCallServiceImpl extends ZegoCallService {

    private static final String TAG = "CallServiceImpl";

    @Override
    public void callUser(String userID, ZegoCallType callType, String createRoomToken, ZegoCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            String callID = selfUserID + System.currentTimeMillis();
            List<String> target = Collections.singletonList(userID);
            ZegoCallCommand callCommand = new ZegoCallCommand();
            callCommand.putParameter("selfUserID", selfUserID);
            callCommand.putParameter("callID", callID);
            callCommand.putParameter("callees", target);
            callCommand.putParameter("callType", callType);
            callCommand.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (errorCode == 0) {
                        ZegoCallInfo callInfo = new ZegoCallInfo();
                        callInfo.callID = callID;
                        callInfo.caller = userService.localUserInfo;
                        setCallInfo(callInfo);
                    }
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
            command.putParameter("selfUserID", userService.localUserInfo.userID);
            command.putParameter("userID", userID);
            command.putParameter("callID", getCallInfo().callID);
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (errorCode == 0) {
                        setCallInfo(null);
                    }
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
        if (userService.localUserInfo != null) {
            ZegoAcceptCallCommand command = new ZegoAcceptCallCommand();
            command.putParameter("selfUserID", userService.localUserInfo.userID);
            command.putParameter("userID", getCallInfo().caller.userID);
            command.putParameter("callID", getCallInfo().callID);
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
            command.putParameter("userID", userID);
            command.putParameter("selfUserID", userService.localUserInfo.userID);
            command.putParameter("callID", getCallInfo().callID);
            command.putParameter("type", type.getValue());
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (errorCode == 0) {
                        setCallInfo(null);
                    }
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
            ZegoEndCallCommand command = new ZegoEndCallCommand();
            command.putParameter("selfUserID", userService.localUserInfo.userID);
            command.putParameter("callID", getCallInfo().callID);
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (errorCode == 0) {
                        setCallInfo(null);
                    }
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
        // listen for cancel action when receive call
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null && callInfo != null) {
            ZegoListenCallCommand command = new ZegoListenCallCommand();
            command.putParameter("callID", callInfo.callID);
            command.execute(null);
        }
    }

    public ZegoCallServiceImpl() {
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.CANCEL_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                if (getCallInfo().callID.equals(call_id)) {
                    if (listener != null) {
                        listener.onReceiveCallCanceled(getCallInfo().caller, ZegoCancelType.INTENT);
                        setCallInfo(null);
                    }
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.ACCEPT_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                String targetUserID = parameter.get("callee_id");
                if (getCallInfo().callID.equals(call_id)) {
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = targetUserID;
                        listener.onReceiveCallResponse(userInfo, ZegoResponseType.Accept);
                    }
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.DECLINE_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                String targetUserID = parameter.get("callee_id");
                if (getCallInfo().callID.equals(call_id)) {
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = targetUserID;
                        listener.onReceiveCallResponse(userInfo, ZegoResponseType.Reject);
                        setCallInfo(null);
                    }
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.END_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                if (getCallInfo().callID.equals(call_id)) {
                    if (listener != null) {
                        listener.onReceiveCallEnded();
                        setCallInfo(null);
                    }
                }
            }
        });
    }
}
