package im.zego.callsdk.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoLoginCommand;
import im.zego.callsdk.command.ZegoLogoutCommand;
import im.zego.callsdk.command.ZegoUserListCommand;
import im.zego.callsdk.listener.ZegoUserLisCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.List;

public class ZegoUserServiceImpl extends ZegoUserService {

    @Override
    public void login(String authToken, ZegoCallback callback) {
        ZegoLoginCommand command = new ZegoLoginCommand();
        command.putParameter("token", authToken);
        command.execute(new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {
                if (callback != null) {
                    callback.onResult(errorCode);
                }
            }
        });
    }

    @Override
    public void validateAccount() {
        ZegoCommandManager.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        localUserInfo = new ZegoUserInfo();
        localUserInfo.userID = currentUser.getUid();
        localUserInfo.userName = currentUser.getDisplayName();
    }

    @Override
    public void logout() {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            String selfUserID = userService.localUserInfo.userID;
            ZegoLogoutCommand command = new ZegoLogoutCommand();
            command.execute((errorCode, obj) -> {

            });
        }
    }

    @Override
    public void getOnlineUserList(ZegoUserLisCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            ZegoUserListCommand command = new ZegoUserListCommand();
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    List<ZegoUserInfo> onlineUserList = (List<ZegoUserInfo>) obj;
                    callback.onGetUserList(errorCode, onlineUserList);
                }
            });
        } else {
            if (callback != null) {
                callback.onGetUserList(-1000, new ArrayList<>());
            }
        }
    }

}
