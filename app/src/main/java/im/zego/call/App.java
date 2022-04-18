package im.zego.call;

import android.app.Activity;
import android.app.Application;
import android.view.Gravity;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.tencent.mmkv.MMKV;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.token.ZegoTokenManager;
import im.zego.callsdk.callback.ZegoTokenCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.ZegoCallTokenDelegate;
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

        ZegoCallManager.getInstance().setTokenDelegate(new ZegoCallTokenDelegate() {
            @Override
            public void getToken(@NonNull String userID, @NonNull ZegoTokenCallback callback) {
                ZegoTokenManager.getInstance().getToken(userID, callback);
            }
        });

        AppUtils.registerAppStatusChangedListener(new Utils.OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                ZegoCallManager.getInstance().dismissNotification(activity);
                for (Activity activity1 : ActivityUtils.getActivityList()) {
                    if (activity1 instanceof CallActivity) {
                        ActivityUtils.startActivity(CallActivity.class);
                        break;
                    }
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
