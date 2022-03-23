package im.zego.call;

import android.app.Application;
import com.blankj.utilcode.util.Utils;
import com.tencent.mmkv.MMKV;
import im.zego.call.auth.AuthInfoManager;
import im.zego.callsdk.service.ZegoRoomManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        AuthInfoManager.getInstance().init(this);

        MMKV.initialize(this);

        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoRoomManager.getInstance().init(appID, this);
    }
}
