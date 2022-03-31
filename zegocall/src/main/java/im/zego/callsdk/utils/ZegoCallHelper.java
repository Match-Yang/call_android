package im.zego.callsdk.utils;

import androidx.annotation.NonNull;

import java.util.Objects;

import im.zego.callsdk.service.ZegoCallService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;


public class ZegoCallHelper {

    @NonNull
    public static boolean isUserIDSelf(String userID) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        return Objects.equals(userService.getLocalUserInfo().userID, userID);
    }

    @NonNull
    public static String getSelfStreamID() {
        String selfUserID = ZegoServiceManager.getInstance().userService.getLocalUserInfo().userID;
        return getStreamID(selfUserID);
    }

    @NonNull
    public static String getStreamID(String userID) {
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        String roomID = callService.getCallInfo().callID;
        return String.format("%s_%s_%s", roomID, userID, "main");
    }

    @NonNull
    public static String getStreamID(String userID, String roomID) {
        return String.format("%s_%s_%s", roomID, userID, "main");
    }

    @NonNull
    public static String getUserID(String streamID) {
        return streamID.split("_")[1];
    }
}