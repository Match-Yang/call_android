package im.zego.call.ui.login;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.tencent.mmkv.MMKV;
import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.databinding.ActivityLoginBinding;
import im.zego.call.http.CallApi;
import im.zego.call.http.IAsyncGetCallback;
import im.zego.call.http.WebClientManager;
import im.zego.call.http.bean.UserBean;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.common.LoadingDialog;
import im.zego.call.ui.entry.EntryActivity;
import im.zego.call.utils.PermissionHelper;
import im.zego.call.utils.PermissionHelper.IPermissionCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import java.util.Random;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private static final String TAG = "LoginActivity";
    private boolean isRequestingUserID = false;
    private boolean isLogin = false;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: isTaskRoot() = [" + isTaskRoot() + "]");
        if (!isTaskRoot()) {
            finish();
            return;
        }

        ImmersionBar.with(this).reset().init();

        int fontHeight = binding.welcomeText.getPaint().getFontMetricsInt(null);
        binding.welcomeText.setLineSpacing(SizeUtils.dp2px(40f) - fontHeight, 1);

        binding.loginButton.setEnabled(false);
        binding.loginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.loginButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
                    @Override
                    public void onRequestCallback(boolean isAllGranted) {
                        if (isAllGranted) {
                            onLoginButtonClicked();
                        }
                    }
                });
            }
        });

        MMKV kv = MMKV.defaultMMKV();
        String userName = kv.decodeString("userName");
        Log.d(TAG, "onCreate() called with: userName = [" + kv.decodeString("userName") + "],autoLogin:" + (kv
            .decodeBool("autoLogin")));
        if (TextUtils.isEmpty(userName)) {
            int nextInt = Math.abs(new Random().nextInt(100));
            String manufacturer = DeviceUtils.getManufacturer();
            binding.loginUsername.setText(manufacturer + nextInt);
        } else {
            binding.loginUsername.setText(userName);
            boolean autoLogin = kv.decodeBool("autoLogin");
            if (autoLogin) {
                PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
                    @Override
                    public void onRequestCallback(boolean isAllGranted) {
                        if (isAllGranted) {
                            onLoginButtonClicked();
                        }
                    }
                });
            }
        }

        systemPermissionCheck();
    }

    private void systemPermissionCheck() {
        PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
            @Override
            public void onRequestCallback(boolean isAllGranted) {
                if (isAllGranted) {

                }
            }
        });
    }

    private void onLoginButtonClicked() {
        String userName = binding.loginUsername.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            TextView inputTips = binding.loginInputTips;
            inputTips.setText(R.string.login_page_input_user_name_tip);
            inputTips.setVisibility(View.VISIBLE);
            Handler rootHandler = binding.getRoot().getHandler();
            rootHandler.removeCallbacksAndMessages(null);
            rootHandler.postDelayed(() -> {
                inputTips.setVisibility(View.INVISIBLE);
            }, 2000);
            return;
        }
        MMKV kv = MMKV.defaultMMKV();
        String userID = kv.decodeString("userID");

        showLoading();
        if (TextUtils.isEmpty(userID)) {
            if (!isRequestingUserID) {
                isRequestingUserID = true;
                CallApi.createUser(new IAsyncGetCallback<String>() {
                    @Override
                    public void onResponse(int errorCode, @NonNull String message, String returnedID) {
                        Log.d(TAG,
                            "createUser() called with: errorCode = [" + errorCode + "], message = [" + message
                                + "], returnedID = [" + returnedID + "]");
                        isRequestingUserID = false;
                        if (errorCode == 0) {
                            kv.encode("userID", returnedID);
                            login(userName, returnedID);
                        } else {
                            dismissLoading();
                            ToastUtils.showShort(getString(R.string.create_user_failed, errorCode));
                        }
                    }
                });
            }
        } else {
            login(userName, userID);
        }
    }

    private void login(String userName, String userID) {
        if (isLogin) {
            return;
        }
        isLogin = true;
        WebClientManager.getInstance().login(userName, userID, new IAsyncGetCallback<UserBean>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, UserBean response) {
                Log.d(TAG,
                    "login() called with: errorCode = [" + errorCode + "], message = [" + message
                        + "], response = [" + response + "]");
                isLogin = false;
                dismissLoading();
                MMKV kv = MMKV.defaultMMKV();
                if (errorCode == 0) {
                    kv.encode("autoLogin", true);
                    kv.encode("userName", userName);
                    ZegoUserInfo userInfo = new ZegoUserInfo();
                    userInfo.userName = userName;
                    userInfo.userID = userID;
                    String token = AuthInfoManager.getInstance().generateLoginToken(userID);
                    ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                    userService.login(userInfo, token, code -> {
                        Log.d(TAG, "login: " + code);
                        if (code == 0) {
                            ActivityUtils.startActivity(EntryActivity.class);
                        } else {
                            showWarnTips(getString(R.string.toast_login_fail, code));
                        }
                    });
                } else {
                    WebClientManager.getInstance().logout(userID, null);
                    kv.encode("autoLogin", false);
                    showWarnTips(getString(R.string.toast_login_fail, errorCode));
                }
            }
        });
    }

    private void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        // some brands kill process will not really kill process,
        // which cause login twice
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        String userID = userService.localUserInfo.userID;
        userService.logout();
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
        WebClientManager.getInstance().logout(userID, null);
    }
}

