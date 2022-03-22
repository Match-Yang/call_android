package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoUserInfo;
import java.util.List;

public interface ZegoUserLisCallback {

    void onGetUserList(int errorCode, List<ZegoUserInfo> userInfoList);
}
