package im.zego.call.ui.login;

import static im.zego.call.ui.setting.SettingActivity.TERMS_OF_SERVICE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tencent.mmkv.MMKV;

import im.zego.call.R;
import im.zego.calluikit.ZegoCallManager;
import im.zego.call.databinding.ActivityGoogleLoginBinding;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.call.ui.entry.EntryActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.calluikit.utils.PermissionHelper;
import im.zego.calluikit.utils.TokenManager;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;

public class GoogleLoginActivity extends BaseActivity<ActivityGoogleLoginBinding> {
    private static final String TAG = "GoogleLoginActivity";
    private boolean isTermsChecked = false;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String CLIENT_ID = "637474508182-1hoov11svfvsp7vi0onnh6vmjl0rl4rv.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
//        ImmersionBar.with(this).reset().init();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.loginButton.setOnClickListener(v -> {
            if (!isTermsChecked) {
                ToastUtils.showShort(R.string.toast_login_service_privacy);
                return;
            }
            PermissionHelper.requestCameraAndAudio(GoogleLoginActivity.this, isAllGranted -> {
                if (isAllGranted) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                        userService.setLocalUser(currentUser.getUid(), currentUser.getDisplayName());

                        userService.getOnlineUserList(null);
                        TokenManager.getInstance();
                        ActivityUtils.startActivity(EntryActivity.class);
                    } else {
                        signIn();
                    }
                }
            });
        });
        binding.termsServiceTv.setOnClickListener(v -> {
            WebViewActivity.startWebViewActivity(TERMS_OF_SERVICE);
        });
        binding.termsServiceCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTermsChecked = isChecked;
        });

        systemPermissionCheck();
    }

    private void systemPermissionCheck() {
        PermissionHelper.requestCameraAndAudio(GoogleLoginActivity.this, isAllGranted -> {
            if (isAllGranted) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                    userService.setLocalUser(currentUser.getUid(), currentUser.getDisplayName());
                    ActivityUtils.startActivity(EntryActivity.class);

                    userService.getOnlineUserList(null);
                    TokenManager.getInstance();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
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
                ZegoCallManager.getInstance().callKitService.login(account.getIdToken(), errorCode -> {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");

        // some brands kill process will not really kill process,
        // which cause login twice
//        ZegoCallManager.getInstance().callKitService.logout();
    }
}