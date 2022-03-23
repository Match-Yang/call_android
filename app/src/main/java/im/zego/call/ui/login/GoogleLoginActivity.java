package im.zego.call.ui.login;

import static im.zego.call.ui.setting.SettingActivity.TERMS_OF_SERVICE;

import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;

import im.zego.call.R;
import im.zego.call.ZegoCallKit;
import im.zego.call.databinding.ActivityGoogleLoginBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.PermissionHelper;

public class GoogleLoginActivity extends BaseActivity<ActivityGoogleLoginBinding> {
    private static final String TAG = "GoogleLoginActivity";
    private boolean isTermsChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        ImmersionBar.with(this).reset().init();

        binding.loginButton.setOnClickListener(v -> {
            if (!isTermsChecked) {
                ToastUtils.showShort(R.string.toast_login_service_privacy);
                return;
            }
            PermissionHelper.requestCameraAndAudio(GoogleLoginActivity.this, isAllGranted -> {
                if (isAllGranted) {
                    onLoginButtonClicked();
                }
            });
        });
        binding.termsServiceTv.setOnClickListener(v -> {
            WebViewActivity.startWebViewActivity(TERMS_OF_SERVICE);
        });

//        Drawable drawable = ResourceUtils.getDrawable(R.drawable.terms_service_checkbox);
//        int size = SizeUtils.dp2px(12);
//        drawable.setBounds(0, 0, size, size);
//        binding.termsServiceCheckbox.setCompoundDrawables(null, drawable, null, null);
        binding.termsServiceCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTermsChecked = isChecked;
        });
    }

    private void onLoginButtonClicked() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        ZegoCallKit.getInstance().logout();

//        String userID = ZegoCallKit.getInstance().getLocalUserInfo().userID;
//        WebClientManager.getInstance().logout(userID, null);
    }
}