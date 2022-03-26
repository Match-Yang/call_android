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

import im.zego.call.R;
import im.zego.call.ZegoCallKit;
import im.zego.call.databinding.ActivityGoogleLoginBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.entry.EntryActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.PermissionHelper;
import im.zego.callsdk.service.ZegoListenerManager;

public class GoogleLoginActivity extends BaseActivity<ActivityGoogleLoginBinding> {
    private static final String TAG = "GoogleLoginActivity";
    private boolean isTermsChecked = false;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
//        ImmersionBar.with(this).reset().init();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
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
                    if (currentUser == null) {
                        signIn();
                    } else {
                        ZegoCallKit.getInstance().uiKitService.validateAccount();
                        ActivityUtils.startActivity(EntryActivity.class);
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
        ZegoListenerManager.getInstance();
    }

    private void systemPermissionCheck() {
        PermissionHelper.requestCameraAndAudio(GoogleLoginActivity.this, isAllGranted -> {
            if (isAllGranted) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    ZegoCallKit.getInstance().uiKitService.validateAccount();
                    ActivityUtils.startActivity(EntryActivity.class);
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
                ZegoCallKit.getInstance().uiKitService.login(account.getIdToken(), errorCode -> {
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
//        ZegoCallKit.getInstance().uiKitService.logout();
    }
}