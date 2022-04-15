package im.zego.callsdk.core.interfaceimpl;

import android.text.TextUtils;

import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.utils.ZegoCallErrorCode;
import im.zego.callsdk.utils.CallUtils;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.Objects;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.core.commands.ZegoGetTokenCommand;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.ZegoCallHelper;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;

public class ZegoUserServiceImpl extends ZegoUserService {

    @Override
    public void getToken(String userID, long effectiveTime, ZegoRequestCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.getLocalUserInfo() != null) {
            ZegoGetTokenCommand command = new ZegoGetTokenCommand();
            command.putParameter("userID", userID);
            command.putParameter("effectiveTime", effectiveTime);
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    if (callback != null) {
                        callback.onResult(errorCode, obj);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onResult(ZegoCallErrorCode.ZegoErrorNotLogin, null);
            }
        }
    }

    @Override
    public ZegoUserInfo getLocalUserInfo() {
        return localUserInfo;
    }

    @Override
    public void setLocalUser(String userID, String userName) {
        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            return;
        }
        if (userID.length() > 64) {
            CallUtils.e("setLocalUser: userID's length more than 64");
            userID = userID.substring(0, 63);
        }
        ZegoUserInfo userInfo = new ZegoUserInfo();
        userInfo.userID = userID;
        userInfo.userName = userName;
        localUserInfo = userInfo;
    }

    @Override
    public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        String userID = ZegoCallHelper.getUserID(streamID);
        CallUtils.d("onRemoteMicStateUpdate() called with: userID = [" + userID + "], state = [" + state + "]");
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
    public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        String userID = ZegoCallHelper.getUserID(streamID);
        CallUtils.d(
            "onRemoteCameraStateUpdate() called with: userID = [" + userID + "], state = [" + state + "]");
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

    public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
        if (updateType == ZegoUpdateType.ADD) {
            for (ZegoUser zegoUser : userList) {
                ZegoUserInfo userInfo = new ZegoUserInfo();
                userInfo.userID = zegoUser.userID;
                userInfo.userName = zegoUser.userName;
                userInfoList.add(userInfo);
            }
        } else {
            for (ZegoUser zegoUser : userList) {
                ZegoUserInfo userInfo = new ZegoUserInfo();
                userInfo.userID = zegoUser.userID;
                userInfo.userName = zegoUser.userName;
                userInfoList.remove(userInfo);
            }
        }
    }
}
