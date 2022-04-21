package im.zego.call;

import android.app.Application;
import android.util.Log;
import android.view.Gravity;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.token.ZegoTokenManager;
import im.zego.callsdk.callback.ZegoTokenCallback;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.ZegoTokenProvider;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

        AuthInfoManager.getInstance().init(this);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoCallManager.getInstance().init(appID, this, new ZegoTokenProvider() {
            @Override
            public void getToken(String userID, ZegoTokenCallback callback) {
                ZegoTokenManager.getInstance().getToken(userID, false, callback);
            }
        });

        ToastUtils.getDefaultMaker()
            .setGravity(Gravity.CENTER, 0, 0)
            .setTextColor(ColorUtils.getColor(R.color.white))
            .setBgColor(ColorUtils.getColor(R.color.dark_black));

        Log.d("Application", "Application onCreate() called:" + BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
    }
}
