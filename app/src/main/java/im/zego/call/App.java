package im.zego.call;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.tencent.mmkv.MMKV;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        MMKV.initialize(this);

        ZegoCallKit.getInstance().init(this);
    }
}
