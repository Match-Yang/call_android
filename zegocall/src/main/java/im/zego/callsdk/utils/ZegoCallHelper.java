package im.zego.callsdk.utils;

import androidx.annotation.NonNull;

import java.util.Objects;

import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;


public class ZegoCallHelper {

    @NonNull
    public static boolean isUserIDSelf(String userID) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        return Objects.equals(userService.localUserInfo.userID, userID);
    }

    @NonNull
    public static String getSelfStreamID() {
        String selfUserID = ZegoServiceManager.getInstance().userService.localUserInfo.userID;
        return getStreamID(selfUserID);
    }
    @NonNull
    public static String getStreamID(String userID) {
        String roomID = ZegoServiceManager.getInstance().roomService.roomInfo.roomID;
        return String.format("%s_%s_%s", roomID, userID, "main");
    }
}