package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoGetTokenCommand;
import im.zego.callsdk.command.ZegoGetUserCommand;
import im.zego.callsdk.command.ZegoLoginCommand;
import im.zego.callsdk.command.ZegoLogoutCommand;
import im.zego.callsdk.command.ZegoUserListCommand;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (userService.localUserInfo != null) {
            ZegoUserListCommand command = new ZegoUserListCommand();
            command.execute(new ZegoRequestCallback() {
                @Override
                public void onResult(int errorCode, Object obj) {
                    userInfoList = (List<ZegoUserInfo>) obj;
                    callback.onGetUserList(errorCode, userInfoList);
                }
            });
        } else {
            if (callback != null) {
                callback.onGetUserList(-1000, new ArrayList<>());
            }
        }
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
        CountDownLatch latch = new CountDownLatch(1);
        ZegoGetUserCommand command = new ZegoGetUserCommand();
        command.execute(new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {
                localUserInfo = (ZegoUserInfo) obj;
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return localUserInfo;
    }

}
