package im.zego.call.ui.setting;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.call.BuildConfig;
import im.zego.call.R;
import im.zego.call.UIKitActivity;
import im.zego.call.databinding.ActivitySettingBinding;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.call.ui.login.GoogleLoginActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.callsdk.model.ZegoCallErrorCode;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.utils.TokenManager;
import im.zego.zegoexpress.ZegoExpressEngine;

public class SettingActivity extends UIKitActivity<ActivitySettingBinding> {

    public static final String TERMS_OF_SERVICE = "https://www.zegocloud.com/policy?index=1";
    public static final String PRIVACY_POLICY = "https://www.zegocloud.com/policy?index=0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZegoUserInfo localUserInfo = ZegoCallManager.getInstance().getLocalUserInfo();
        if (localUserInfo == null) {
            return;
        }
        binding.settingTitleBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.expressSdkVersion.setText(ZegoExpressEngine.getVersion());
        binding.appVersion.setText(BuildConfig.VERSION_NAME);

        binding.termsService.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(TERMS_OF_SERVICE);
            }
        });

        binding.privacyPolicy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(PRIVACY_POLICY);
            }
        });

        binding.uploadLog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoCallManager.getInstance().uploadLog(errorCode -> {
                    if (errorCode == ZegoCallErrorCode.SUCCESS) {
                        showNormalTips(getString(R.string.toast_upload_log_success));
                    } else {
                        showWarnTips(getString(R.string.toast_upload_log_fail, errorCode));
                    }
                });
            }
        });

        binding.logOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUserManager.getInstance().signOutFirebaseAuth();
                ZegoCallManager.getInstance().callKitService.logout();
                TokenManager.getInstance().reset();
                ActivityUtils.finishToActivity(GoogleLoginActivity.class, false);
            }
        });
    }
}