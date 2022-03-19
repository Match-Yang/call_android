package im.zego.call.ui.entry;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.Utils.OnAppStatusChangedListener;
import com.tencent.mmkv.MMKV;

import im.zego.call.ZegoCallKit;
import im.zego.call.databinding.ActivityEntryBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.login.LoginActivity;
import im.zego.call.ui.setting.SettingActivity;
import im.zego.call.ui.user.OnlineUserActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;

public class EntryActivity extends BaseActivity<ActivityEntryBinding> {

    public static final String URL_GET_MORE = "https://www.zegocloud.com/";
    public static final String URL_CONTACT_US = "https://www.zegocloud.com/talk";
    private static final String TAG = "EntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZegoCallKit.getInstance().startListen(this);

        binding.entrySetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(SettingActivity.class);
            }
        });
        binding.entryContactUs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_CONTACT_US);
            }
        });
        binding.entryGetMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_GET_MORE);
            }
        });
        binding.entryBannerCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(OnlineUserActivity.class);
            }
        });

        ZegoUserInfo localUserInfo = ZegoCallKit.getInstance().getLocalUserInfo();
        binding.entryUserId.setText("ID:" + localUserInfo.userID);
        binding.entryUserName.setText(localUserInfo.userName);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(localUserInfo.userName);
        binding.entryUserAvatar.setImageDrawable(userIcon);

        AppUtils.registerAppStatusChangedListener(new OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                ZegoCallKit.getInstance().dismissNotification(activity);
                // some phone will freeze app when phone is desktop,even if we start foreground service,
                // such as vivo.
                // so when app back to foreground, if heartbeat failed,relogin.
//                WebClientManager.getInstance().tryReLogin((errorCode, message, response) -> {
//                    if (errorCode != 0) {
//                        logout();
//                    }
//                });
            }

            @Override
            public void onBackground(Activity activity) {
                boolean needNotification = CallStateManager.getInstance().isInACallStream();
                ZegoUserInfo userInfo = CallStateManager.getInstance().getUserInfo();
                if (needNotification && userInfo != null) {
                    ZegoCallKit.getInstance().showNotification(userInfo);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        ZegoCallKit.getInstance().stopListen(this);
    }

    @Override
    public void onBackPressed() {

    }

    private void logout() {
        ZegoCallKit.getInstance().logout();

        MMKV.defaultMMKV().encode("autoLogin", false);
        ActivityUtils.finishToActivity(LoginActivity.class, false);
    }
}