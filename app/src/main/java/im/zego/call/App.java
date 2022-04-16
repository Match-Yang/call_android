package im.zego.call;

import android.app.Activity;
import android.app.Application;
import android.view.Gravity;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.tencent.mmkv.MMKV;
import im.zego.call.auth.AuthInfoManager;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.ui.call.CallActivity;
import im.zego.calluikit.ui.call.CallStateManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        MMKV.initialize(this);

        AuthInfoManager.getInstance().init(this);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoCallManager.getInstance().init(appID, this);

        ToastUtils.getDefaultMaker()
                .setGravity(Gravity.CENTER, 0, 0)
                .setTextColor(ColorUtils.getColor(R.color.white))
                .setBgColor(ColorUtils.getColor(R.color.dark_black));

        AppUtils.registerAppStatusChangedListener(new Utils.OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                ZegoCallManager.getInstance().dismissNotification(activity);
                if (CallStateManager.getInstance().isInACallStream()) {
                    ActivityUtils.startActivity(CallActivity.class);
                }
            }

            @Override
            public void onBackground(Activity activity) {
                boolean needNotification = CallStateManager.getInstance().isInACallStream();
                ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                if (needNotification && userInfo != null) {
                    ZegoCallManager.getInstance().showNotification(userInfo);
                }
            }
        });
    }
}
