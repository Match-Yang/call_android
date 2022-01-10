package im.zego.call;

import android.app.Application;
import com.blankj.utilcode.util.Utils;
import im.zego.call.auth.AuthInfoManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        AuthInfoManager.getInstance().init(this);
    }
}
