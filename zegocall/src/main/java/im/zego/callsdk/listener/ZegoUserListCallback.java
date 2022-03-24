package im.zego.callsdk.listener;

import im.zego.callsdk.model.ZegoUserInfo;
import java.util.List;

public interface ZegoUserListCallback {

    void onGetUserList(int errorCode, List<ZegoUserInfo> userInfoList);
}
