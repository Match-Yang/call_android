package im.zego.call.ui.login;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImmersionBar.with(this).reset().init();

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
        int nextInt = Math.abs(new Random().nextInt());
        String manufacturer = DeviceUtils.getManufacturer();
        binding.loginUsername.setText(manufacturer + nextInt);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.isGrantedDrawOverlays()) {
                Builder builder = new Builder(this);
                builder.setMessage("to show call on desktop we need overlay permission,"
                    + "or you may miss some call");
                builder.setPositiveButton(R.string.dialog_room_page_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.requestDrawOverlays(new SimpleCallback() {
                            @Override
                            public void onGranted() {

                            }

                            @Override
                            public void onDenied() {

                            }
                        });
                    }
                });
                builder.create().show();
            }
        }
    }

    private void onLoginButtonClicked() {
        String userName = binding.loginUsername.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            TextView inputTips = binding.loginInputTips;
            inputTips.setText(R.string.name_format_error);
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
                        }
                    }
                });
            }
        } else {
            login(userName, userID);
        }
    }

    private void login(String userName, String userID) {
        CallApi.login(userName, userID, new IAsyncGetCallback<UserBean>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, UserBean response) {
                Log.d(TAG,
                    "login() called with: errorCode = [" + errorCode + "], message = [" + message
                        + "], response = [" + response + "]");
                if (errorCode == 0) {
                    WebClientManager.getInstance().startHeartBeat(userID);
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
                            WebClientManager.getInstance().stopHeartBeat();
                        }
                    });
                } else {
                    CallApi.logout(userID, null);
                    WebClientManager.getInstance().stopHeartBeat();
                    showWarnTips("login error,errorCode :" + errorCode);
                }
            }
        });
    }
}

