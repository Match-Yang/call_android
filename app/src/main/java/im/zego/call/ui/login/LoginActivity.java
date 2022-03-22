package im.zego.call.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gyf.immersionbar.ImmersionBar;
import com.tencent.mmkv.MMKV;
import im.zego.call.R;
import im.zego.call.databinding.ActivityLoginBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.common.LoadingDialog;
import im.zego.call.ui.entry.EntryActivity;
import im.zego.call.utils.PermissionHelper;
import im.zego.call.utils.PermissionHelper.IPermissionCallback;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.service.ZegoListenerManager;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private static final String TAG = "LoginActivity";
    private LoadingDialog loadingDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
                    @Override
                    public void onRequestCallback(boolean isAllGranted) {
                        if (isAllGranted) {
                            //                            onLoginButtonClicked();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser == null) {
                                signIn();
                            }else {
                                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                                userService.validateAccount();
                                ActivityUtils.startActivity(EntryActivity.class);
                            }
                        }
                    }
                });
            }
        });
        systemPermissionCheck();
        ZegoListenerManager.getInstance();
    }

    private void systemPermissionCheck() {
        PermissionHelper.requestCameraAndAudio(LoginActivity.this, new IPermissionCallback() {
            @Override
            public void onRequestCallback(boolean isAllGranted) {
                if (isAllGranted) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                        userService.validateAccount();
                        ActivityUtils.startActivity(EntryActivity.class);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                showLoading();
                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                userService.login(null, account.getIdToken(), errorCode -> {
                    dismissLoading();
                    if (errorCode == 0) {
                        ActivityUtils.startActivity(EntryActivity.class);
                    } else {
                        showWarnTips(getString(R.string.toast_login_fail, errorCode));
                    }
                });
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                showWarnTips(getString(R.string.toast_login_fail, -1000));
            }
        }
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
        //        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        //        String userID = userService.localUserInfo.userID;
        //        userService.logout();
        //        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
        //        WebClientManager.getInstance().logout(userID, null);
    }
}

