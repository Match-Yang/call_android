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

import im.zego.call.UIKitActivity;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.calluikit.ZegoCallManager;
import im.zego.call.databinding.ActivityEntryBinding;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.call.ui.login.GoogleLoginActivity;
import im.zego.call.ui.setting.SettingActivity;
import im.zego.call.ui.user.OnlineUserActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.calluikit.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;

public class EntryActivity extends UIKitActivity<ActivityEntryBinding> {

    public static final String URL_GET_MORE = "https://www.zegocloud.com/";
    public static final String URL_CONTACT_US = "https://www.zegocloud.com/talk";
    private static final String TAG = "EntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZegoCallManager.getInstance().startListen(this);

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

        ZegoUserInfo localUserInfo = ZegoCallManager.getInstance().getLocalUserInfo();
        if (localUserInfo == null) {
            logout();
            return;
        }
        binding.entryUserId.setText("ID:" + localUserInfo.userID);
        binding.entryUserName.setText(localUserInfo.userName);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(localUserInfo.userName);
        binding.entryUserAvatar.setImageDrawable(userIcon);

        AppUtils.registerAppStatusChangedListener(new OnAppStatusChangedListener() {
            @Override
            public void onForeground(Activity activity) {
                ZegoCallManager.getInstance().dismissNotification(activity);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        ZegoCallManager.getInstance().stopListen(this);
    }

    @Override
    public void onBackPressed() {

    }

    private void logout() {
        FirebaseUserManager.getInstance().signOutFirebaseAuth();
        ZegoCallManager.getInstance().callKitService.logout();
        ActivityUtils.finishToActivity(GoogleLoginActivity.class, false);
    }
}