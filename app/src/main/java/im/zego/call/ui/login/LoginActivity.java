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
import com.blankj.utilcode.util.StringUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        if (TextUtils.isEmpty(userName)) {
            int nextInt = Math.abs(new Random().nextInt(100));
            String manufacturer = DeviceUtils.getManufacturer();
            binding.loginUsername.setText(manufacturer + nextInt);
        } else {
            binding.loginUsername.setText(userName);
            PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
                @Override
                public void onRequestCallback(boolean isAllGranted) {
                    if (isAllGranted) {
                        onLoginButtonClicked();
                    }
                }
            });
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
                            showWarnTips(getString(R.string.create_user_failed, errorCode));
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
        CallApi.login(userName, userID, new IAsyncGetCallback<UserBean>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, UserBean response) {
                Log.d(TAG,
                    "login() called with: errorCode = [" + errorCode + "], message = [" + message
                        + "], response = [" + response + "]");
                isLogin = false;
                if (errorCode == 0) {
                    MMKV kv = MMKV.defaultMMKV();
                    kv.encode("login", true);
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
                    CallApi.logout(userID, null);
                    showWarnTips(getString(R.string.toast_login_fail, errorCode));
                }
            }
        });
    }
}

