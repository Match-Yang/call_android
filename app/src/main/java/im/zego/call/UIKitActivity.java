package im.zego.call;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.viewbinding.ViewBinding;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.call.ui.login.GoogleLoginActivity;
import im.zego.calluikit.ui.BaseActivity;

public class UIKitActivity<T extends ViewBinding> extends BaseActivity<T> {

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUserManager.getInstance().setSignInOtherDeviceListener(() -> {
            ActivityUtils.finishToActivity(GoogleLoginActivity.class, false);
            Activity loginActivity = null;
            for (Activity activity : ActivityUtils.getActivityList()) {
                if (activity instanceof GoogleLoginActivity) {
                    loginActivity = activity;
                    break;
                }
            }
            if (loginActivity != null) {
                AlertDialog.Builder builder = new Builder(loginActivity);
                builder.setMessage(R.string.toast_kickout_error);
                builder.setPositiveButton(R.string.dialog_login_page_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityUtils.finishToActivity(GoogleLoginActivity.class, false);
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            } else {
                ToastUtils.showLong(R.string.toast_kickout_error);
            }
        });
    }
}
