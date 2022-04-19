package im.zego.call.ui.login;

import static im.zego.call.ui.setting.SettingActivity.PRIVACY_POLICY;
import static im.zego.call.ui.setting.SettingActivity.TERMS_OF_SERVICE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.SizeUtils;
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
import im.zego.call.databinding.ActivityGoogleLoginBinding;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.call.ui.entry.EntryActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.calluikit.utils.PermissionHelper;

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
                    FirebaseUser currentUser = FirebaseUserManager.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                        userService.setLocalUser(currentUser.getUid(), currentUser.getDisplayName());
                        ActivityUtils.startActivity(EntryActivity.class);
                    } else {
                        signIn();
                    }
                }
            });
        });

        String termsOfUseString = getString(R.string.login_page_terms_of_service);
        String privacyPolicyString = getString(R.string.login_page_privacy_policy);
        String content = getString(R.string.login_page_service_privacy);
        SpannableString spannableString = SpannableString.valueOf(content);
        int termsOfUseIndex = content.indexOf(termsOfUseString);
        int privacyPolicyIndex = content.indexOf(privacyPolicyString);
        int color = Color.parseColor("#0055ff");
        spannableString.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    WebViewActivity.startWebViewActivity(TERMS_OF_SERVICE);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(color);
                    ds.setUnderlineText(false);
                }
            },
            termsOfUseIndex, termsOfUseString.length() + termsOfUseIndex, 33
        );
        spannableString.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    WebViewActivity.startWebViewActivity(PRIVACY_POLICY);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(color);
                    ds.setUnderlineText(false);
                }
            }, privacyPolicyIndex, privacyPolicyString.length() + privacyPolicyIndex, 33
        );
        binding.termsServiceTv.setText(spannableString);
        binding.termsServiceTv.setMovementMethod(LinkMovementMethod.getInstance());

        binding.termsServiceCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SPStaticUtils.put(Constants.ZEGO_IS_TERMS_CHECKED_KEY, isChecked, true);
            isTermsChecked = isChecked;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTermsChecked = SPStaticUtils.getBoolean(Constants.ZEGO_IS_TERMS_CHECKED_KEY);
        binding.termsServiceCheckbox.setChecked(isTermsChecked);
    }

    private void systemPermissionCheck() {
        PermissionHelper.requestCameraAndAudio(GoogleLoginActivity.this, isAllGranted -> {
            if (isAllGranted) {
                FirebaseUser currentUser = FirebaseUserManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                    userService.setLocalUser(currentUser.getUid(), currentUser.getDisplayName());
                    ActivityUtils.startActivity(EntryActivity.class);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseUserManager.getInstance().getCurrentUser();
        Log.d(TAG, "onStart() called,currentUser:" + currentUser);
        if (currentUser == null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            });
        }
        systemPermissionCheck();
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
                showLoading();
                FirebaseUserManager.getInstance().signInFirebase(account.getIdToken(), errorCode -> {
                    dismissLoading();
                    if (errorCode == 0) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                        userService.setLocalUser(currentUser.getUid(), currentUser.getDisplayName());
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