package im.zego.callsdk.service;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoGetTokenCommand;
import im.zego.callsdk.command.ZegoLoginCommand;
import im.zego.callsdk.command.ZegoLogoutCommand;
import im.zego.callsdk.command.ZegoUserListCommand;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CoreTest;
import im.zego.callsdk.utils.ZegoCallHelper;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;

public class ZegoUserServiceImpl extends ZegoUserService {

    @Override
    public void login(String authToken, ZegoCallback callback) {
        ZegoLoginCommand command = new ZegoLoginCommand();
        command.putParameter("authToken", authToken);
        command.execute(new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {
                if (errorCode == 0) {
                    Map<String, String> user = (HashMap<String, String>) obj;
                    localUserInfo = new ZegoUserInfo();
                    localUserInfo.userID = user.get("userID");
                    localUserInfo.userName = user.get("userName");
                }
                if (callback != null) {
                    callback.onResult(errorCode);
                }
            }
        });
    }

    @Override
    public void logout() {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoLogoutCommand command = new ZegoLogoutCommand();
            command.putParameter("selfUserID", selfUserID);
            command.execute((errorCode, obj) -> {

            });
        }
    }

    @Override
    public void getOnlineUserList(ZegoUserListCallback callback) {
        ZegoUserListCommand command = new ZegoUserListCommand();
        command.execute(new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {
                userInfoList = (List<ZegoUserInfo>) obj;
                for (ZegoUserInfo userInfo : userInfoList) {
                    if (Objects.equals(userInfo.userID, localUserInfo.userID)) {
                        localUserInfo = userInfo;
                        break;
                    }
                }
                if (callback != null) {
                    callback.onGetUserList(errorCode, userInfoList);
                }
            }
        });
    }

    @Override
    public void getToken(String userID, ZegoRequestCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            ZegoGetTokenCommand command = new ZegoGetTokenCommand();
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                }
            });
        } else {
            if (callback != null) {
            }
        }
    }

    @Override
    public ZegoUserInfo getLocalUserInfo() {
        return localUserInfo;
    }

    @Override
    public void setLocalUser(String userID, String userName) {
        ZegoUserInfo userInfo = new ZegoUserInfo();
        userInfo.userID = userID;
        userInfo.userName = userName;
        localUserInfo = userInfo;
    }

    @Override
    void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        String userID = ZegoCallHelper.getUserID(streamID);
        Log.d(CoreTest.TAG, "onRemoteMicStateUpdate() called with: userID = [" + userID + "], state = [" + state + "]");
        if (state != ZegoRemoteDeviceState.OPEN && state != ZegoRemoteDeviceState.MUTE) {
            return;
        }

        ZegoUserInfo userInfo = null;
        for (ZegoUserInfo zegoUserInfo : userInfoList) {
            if (Objects.equals(zegoUserInfo.userID, userID)) {
                userInfo = zegoUserInfo;
                break;
            }
        }

        if (userInfo != null) {
            userInfo.mic = state == ZegoRemoteDeviceState.OPEN;
        }

        if (Objects.equals(localUserInfo.userID, userID)) {
            localUserInfo.mic = state == ZegoRemoteDeviceState.OPEN;
        }

        if (listener != null) {
            listener.onUserInfoUpdated(userInfo);
        }
    }

    @Override
    void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        String userID = ZegoCallHelper.getUserID(streamID);
        Log.d(CoreTest.TAG, "onRemoteCameraStateUpdate() called with: userID = [" + userID + "], state = [" + state + "]");
        if (state != ZegoRemoteDeviceState.OPEN && state != ZegoRemoteDeviceState.DISABLE) {
            return;
        }

        ZegoUserInfo userInfo = null;
        for (ZegoUserInfo zegoUserInfo : userInfoList) {
            if (Objects.equals(zegoUserInfo.userID, userID)) {
                userInfo = zegoUserInfo;
                break;
            }
        }

        if (userInfo != null) {
            userInfo.camera = state == ZegoRemoteDeviceState.OPEN;
        }

        if (Objects.equals(localUserInfo.userID, userID)) {
            localUserInfo.camera = state == ZegoRemoteDeviceState.OPEN;
        }

        if (listener != null) {
            listener.onUserInfoUpdated(userInfo);
        }
    }
}
