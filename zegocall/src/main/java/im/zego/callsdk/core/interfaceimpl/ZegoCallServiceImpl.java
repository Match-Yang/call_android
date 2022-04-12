package im.zego.callsdk.core.interfaceimpl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoNotifyListener;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.core.commands.ZegoAcceptCallCommand;
import im.zego.callsdk.core.commands.ZegoCallCommand;
import im.zego.callsdk.core.commands.ZegoCancelCallCommand;
import im.zego.callsdk.core.commands.ZegoDeclineCallCommand;
import im.zego.callsdk.core.commands.ZegoEndCallCommand;
import im.zego.callsdk.core.commands.ZegoHeartBeatCommand;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.listener.ZegoListenerManager;
import im.zego.callsdk.model.ZegoCallInfo;
import im.zego.callsdk.model.ZegoCallTimeoutType;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoLocalUserStatus;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.zegoexpress.constants.ZegoRoomState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

public class ZegoCallServiceImpl extends ZegoCallService {

    private static final String TAG = "CallServiceImpl";
    private Timer heartTimer = new Timer();
    private static final int CALL_TIMEOUT = 60 * 1000;
    private Runnable callTimeoutRunnable = new Runnable() {

        @Override
        public void run() {
            handler.removeCallbacks(callTimeoutRunnable);

            if (listener != null) {
                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                if (userService.getLocalUserInfo() != null) {
                    listener.onReceiveCallTimeout(userService.getLocalUserInfo(), ZegoCallTimeoutType.Calling);
                }
                setCallInfo(null);
            }
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper());
    private HashMap<String, ZegoCallback> callbackHashMap = new HashMap<>();

    @Override
    public void callUser(ZegoUserInfo userInfo, ZegoCallType callType, String createRoomToken, ZegoCallback callback) {
        Log.d(TAG,
            "callUser() called with: userInfo = [" + userInfo + "], callType = [" + callType + "], createRoomToken = ["
                + createRoomToken + "], callback = [" + callback + "]");
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.getLocalUserInfo() != null) {
            handler.postDelayed(callTimeoutRunnable, CALL_TIMEOUT);
            String selfUserID = userService.getLocalUserInfo().userID;
            String userName = userService.getLocalUserInfo().userName;
            String callID = selfUserID + System.currentTimeMillis();
            ZegoCallCommand callCommand = new ZegoCallCommand();
            callCommand.putParameter("callID", callID);
            callCommand.putParameter("callType", callType);
            HashMap<String, String> self = new HashMap<>();
            self.put("id", selfUserID);
            self.put("name", userName);
            callCommand.putParameter("caller", self);
            List<HashMap<String, String>> callee = new ArrayList<>();
            HashMap<String, String> user = new HashMap<>();
            user.put("id", userInfo.userID);
            user.put("name", userInfo.userName);
            callee.add(user);
            callCommand.putParameter("callees", callee);
            callCommand.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    Log.d(TAG, "callUser onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                    handler.removeCallbacks(callTimeoutRunnable);
                    if (errorCode == 0) {
                        //                        if (getCallInfo().callID == null) {
                        ZegoCallInfo callInfo = new ZegoCallInfo();
                        callInfo.callID = callID;
                        callInfo.caller = userService.getLocalUserInfo();
                        callInfo.callees = new ArrayList<>();
                        callInfo.callees.add(userInfo);
                        setCallInfo(callInfo);

                        ZegoServiceManager.getInstance().roomService.joinRoom(callID, createRoomToken);
                        callbackHashMap.clear();
                        callbackHashMap.put(callID, callback);
                        status = ZegoLocalUserStatus.Outgoing;
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
    public void cancelCall(ZegoCallback callback) {
        Log.d(TAG, "cancelCall() called with: callback = [" + callback + "]");
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        String callID = getCallInfo().callID;
        if (userService.getLocalUserInfo() != null) {
            if (callID != null) {
                handler.removeCallbacks(callTimeoutRunnable);
                ZegoCancelCallCommand command = new ZegoCancelCallCommand();
                command.putParameter("selfUserID", userService.getLocalUserInfo().userID);
                command.putParameter("userID", getCallInfo().caller.userID);
                command.putParameter("callID", callID);
                command.execute(new ZegoRequestCallback() {
                    @Override
                    public void onResult(int errorCode, Object obj) {
                        Log.d(TAG,
                            "cancelCall onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                    }
                });
                setCallInfo(null);
                ZegoServiceManager.getInstance().roomService.leaveRoom();
            }
            if (callback != null) {
                callback.onResult(0);
            }
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void acceptCall(String joinToken, ZegoCallback callback) {
        Log.d(TAG, "acceptCall() called with: joinToken = [" + joinToken + "], callback = [" + callback + "]");
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        final String callID = getCallInfo().callID;
        if (userService.getLocalUserInfo() != null && callID != null) {
            handler.removeCallbacks(callTimeoutRunnable);
            ZegoAcceptCallCommand command = new ZegoAcceptCallCommand();
            String selfUserID = userService.getLocalUserInfo().userID;
            command.putParameter("selfUserID", selfUserID);
            command.putParameter("userID", getCallInfo().caller.userID);
            command.putParameter("callID", callID);
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    Log.d(TAG,
                        "acceptCall onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                    if (errorCode == 0) {
                        startHeartBeatTimer(callID, selfUserID);
                        ZegoServiceManager.getInstance().roomService.joinRoom(callID, joinToken);
                        callbackHashMap.clear();
                        callbackHashMap.put(callID, callback);
                        status = ZegoLocalUserStatus.Calling;
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
    public void declineCall(ZegoCallback callback) {
        Log.d(TAG, "declineCall() called with: callback = [" + callback + "]");
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        final String callID = getCallInfo().callID;
        if (userService.getLocalUserInfo() != null) {
            if (callID != null) {
                handler.removeCallbacks(callTimeoutRunnable);
                ZegoDeclineCallCommand command = new ZegoDeclineCallCommand();
                command.putParameter("userID", getCallInfo().caller.userID);
                command.putParameter("selfUserID", userService.getLocalUserInfo().userID);
                command.putParameter("type", 1);
                command.putParameter("callID", callID);
                command.execute(new ZegoRequestCallback() {
                    @Override
                    public void onResult(int errorCode, Object obj) {
                        Log.d(TAG,
                            "declineCall onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                    }
                });
                setCallInfo(null);
            }
            if (callback != null) {
                callback.onResult(0);
            }
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void endCall(ZegoCallback callback) {
        ZegoServiceManager.getInstance().roomService.leaveRoom();

        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        Log.d(TAG, "endCall() called with: callback = [" + callback + "]");
        String callID = getCallInfo().callID;
        if (userService.getLocalUserInfo() != null) {
            if (callID != null) {
                handler.removeCallbacks(callTimeoutRunnable);
                ZegoEndCallCommand command = new ZegoEndCallCommand();
                command.putParameter("selfUserID", userService.getLocalUserInfo().userID);
                command.putParameter("callID", callID);
                command.execute(new ZegoRequestCallback() {
                    @Override
                    public void onResult(int errorCode, Object obj) {
                        Log.d(TAG,
                            "endCall onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                    }
                });
                stopHeartBeatTimer();
                setCallInfo(null);
            }
            if (callback != null) {
                callback.onResult(0);
            }
        } else {
            if (callback != null) {
                callback.onResult(-1000);
            }
        }
    }

    @Override
    public void setCallInfo(ZegoCallInfo callInfo) {
        Log.d(TAG, "setCallInfo() called with: callInfo = [" + callInfo + "]");
        if (callInfo == null) {
            this.callInfo = new ZegoCallInfo();
            status = ZegoLocalUserStatus.Free;
        } else {
            this.callInfo = callInfo;
            handler.removeCallbacks(callTimeoutRunnable);
            handler.postDelayed(callTimeoutRunnable, CALL_TIMEOUT);
        }
    }

    private void startHeartBeat(String callID, String userID) {
        Log.d(TAG, "startHeartBeat() called");
        ZegoHeartBeatCommand command = new ZegoHeartBeatCommand();
        command.putParameter("callID", callID);
        command.putParameter("userID", userID);
        command.execute(new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {

            }
        });
    }

    public ZegoCallServiceImpl() {
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.RECEIVE_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Log.d(TAG, "RECEIVE_CALL onNotifyInvoked() called with: obj = [" + obj + "]");
                HashMap<String, Object> data = (HashMap<String, Object>) obj;
                ZegoCallInfo callInfo = new ZegoCallInfo();
                callInfo.callID = (String) data.get("call_id");
                HashMap<String, String> callerData = (HashMap<String, String>) data.get("caller");
                ZegoUserInfo caller = new ZegoUserInfo();
                caller.userID = callerData.get("id");
                caller.userName = callerData.get("name");
                callInfo.caller = caller;
                List<ZegoUserInfo> users = new ArrayList<>();
                List<HashMap<String, String>> calleeData = (List<HashMap<String, String>>) data.get("callees");
                for (HashMap<String, String> hashMap : calleeData) {
                    ZegoUserInfo user = new ZegoUserInfo();
                    user.userID = hashMap.get("id");
                    user.userName = hashMap.get("name");
                    users.add(user);
                }
                callInfo.callees = users;
                ZegoCallType type = ZegoCallType.Voice;
                for (ZegoCallType zegoCallType : ZegoCallType.values()) {
                    if (zegoCallType.getValue() == ((int) data.get("type"))) {
                        type = zegoCallType;
                        break;
                    }
                }
                callInfo.callType = type;
                if (getCallInfo().callID == null) {
                    setCallInfo(callInfo);
                    if (listener != null) {
                        listener.onReceiveCallInvite(callInfo.caller, callInfo.callID, callInfo.callType);
                    }
                } else {
                    Log.d(TAG, "RECEIVE_CALL: getCallInfo().callID != null");
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.CANCEL_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                if (Objects.equals(getCallInfo().callID, call_id)) {
                    handler.removeCallbacks(callTimeoutRunnable);
                    ZegoServiceManager.getInstance().roomService.leaveRoom();
                    status = ZegoLocalUserStatus.Free;
                    if (listener != null) {
                        listener.onReceiveCallCanceled(getCallInfo().caller, ZegoCancelType.INTENT);
                    }
                    setCallInfo(null);
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.ACCEPT_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                String targetUserID = parameter.get("callee_id");
                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                String selfUserID = userService.getLocalUserInfo().userID;
                if (Objects.equals(getCallInfo().callID, call_id)) {
                    handler.removeCallbacks(callTimeoutRunnable);
                    if (Objects.equals(targetUserID, selfUserID)) {
                        // if is self accept,no need to notify again
                        return;
                    }
                    status = ZegoLocalUserStatus.Calling;
                    startHeartBeatTimer(call_id, selfUserID);
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = targetUserID;
                        listener.onReceiveCallAccept(userInfo);
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
                String type = parameter.get("type");
                if (Objects.equals(getCallInfo().callID, call_id)) {
                    handler.removeCallbacks(callTimeoutRunnable);
                    ZegoServiceManager.getInstance().roomService.leaveRoom();
                    status = ZegoLocalUserStatus.Free;
                    ZegoDeclineType declineType;
                    if (Integer.parseInt(type) == ZegoDeclineType.Decline.getValue()) {
                        declineType = ZegoDeclineType.Decline;
                    } else {
                        declineType = ZegoDeclineType.Busy;
                    }
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = targetUserID;
                        listener.onReceiveCallDecline(userInfo, declineType);
                    }
                    setCallInfo(null);
                } else {
                    ZegoDeclineType declineType;
                    if (Integer.parseInt(type) == ZegoDeclineType.Decline.getValue()) {
                        declineType = ZegoDeclineType.Decline;
                    } else {
                        declineType = ZegoDeclineType.Busy;
                    }
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = targetUserID;
                        listener.onReceiveCallDecline(userInfo, declineType);
                    }
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.END_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                String userID = parameter.get("id");
                if (Objects.equals(getCallInfo().callID, call_id)) {
                    handler.removeCallbacks(callTimeoutRunnable);
                    stopHeartBeatTimer();
                    status = ZegoLocalUserStatus.Free;
                    ZegoServiceManager.getInstance().roomService.leaveRoom();
                    if (listener != null) {
                        listener.onReceiveCallEnded();
                    }
                    setCallInfo(null);
                }
            }
        });
        ZegoListenerManager.getInstance().addListener(ZegoListenerManager.TIMEOUT_CALL, new ZegoNotifyListener() {
            @Override
            public void onNotifyInvoked(Object obj) {
                Map<String, String> parameter = (Map<String, String>) obj;
                String call_id = parameter.get("call_id");
                String userID = parameter.get("user_id");
                if (Objects.equals(getCallInfo().callID, call_id)) {
                    handler.removeCallbacks(callTimeoutRunnable);
                    stopHeartBeatTimer();
                    ZegoServiceManager.getInstance().roomService.leaveRoom();
                    status = ZegoLocalUserStatus.Free;
                    if (listener != null) {
                        ZegoUserInfo userInfo = new ZegoUserInfo();
                        userInfo.userID = userID;
                        listener.onReceiveCallTimeout(userInfo, ZegoCallTimeoutType.Connecting);
                        setCallInfo(null);
                    }
                }
            }
        });
    }

    private void startHeartBeatTimer(String callID, String userID) {
        Log.d(TAG, "startHeartBeatTimer() called with: callID = [" + callID + "], userID = [" + userID + "]");
        if (heartTimer != null) {
            heartTimer.cancel();
        }
        heartTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startHeartBeat(callID, userID);
            }
        };
        heartTimer.schedule(task, 0, 24000);
    }

    private void stopHeartBeatTimer() {
        if (heartTimer != null) {
            heartTimer.cancel();
        }
    }

    public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
        Log.d(TAG, "onRoomStateUpdate() called with: roomID = [" + roomID + "], state = [" + state + "], errorCode = ["
            + errorCode + "], extendedData = [" + extendedData + "]");
        if (state == ZegoRoomState.CONNECTED) {
            ZegoCallback zegoCallback = callbackHashMap.remove(roomID);
            if (zegoCallback != null) {
                // join room result
                zegoCallback.onResult(0);
            }
        } else if (state == ZegoRoomState.DISCONNECTED) {
            stopHeartBeatTimer();
            handler.removeCallbacks(callTimeoutRunnable);
            ZegoCallback zegoCallback = callbackHashMap.remove(roomID);
            if (zegoCallback != null) {
                // join room result
                zegoCallback.onResult(errorCode);
                return;
            }
            if (getCallInfo().callID != null) {
                if (listener != null) {
                    ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                    if (userService.getLocalUserInfo() != null) {
                        listener.onReceiveCallTimeout(userService.getLocalUserInfo(), ZegoCallTimeoutType.Connecting);
                    }
                }
                setCallInfo(null);
            }
        }
    }

    @Override
    public void setListener(ZegoCallServiceListener listener) {
        super.setListener(listener);
        if (callInfo.callID != null && listener != null) {
            listener.onReceiveCallInvite(callInfo.caller, callInfo.callID, callInfo.callType);
        }
    }
}
