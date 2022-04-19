package im.zego.calluikit;

import android.app.Application;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;

public interface IZegoCallManager {

    void init(long appID, Application application, ZegoTokenProvider provider);

    void unInit();

    void setLocalUser(String userID, String userName);

    void setListener(ZegoCallManagerListener listener);

    void uploadLog(final ZegoCallback callback);

    void callUser(ZegoUserInfo userInfo, int callState);

    ZegoUserInfo getLocalUserInfo();
}
