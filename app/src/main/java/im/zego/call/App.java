package im.zego.call;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.tencent.mmkv.MMKV;

import im.zego.calluikit.ZegoCallManager;
import im.zego.call.auth.AuthInfoManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        MMKV.initialize(this);

        AuthInfoManager.getInstance().init(this);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoCallManager.getInstance().init(appID, this);
    }
}
