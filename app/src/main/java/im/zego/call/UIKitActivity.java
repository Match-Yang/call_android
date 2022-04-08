package im.zego.call;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.viewbinding.ViewBinding;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.call.ui.login.GoogleLoginActivity;
import im.zego.calluikit.ui.BaseActivity;

public class UIKitActivity<T extends ViewBinding> extends BaseActivity<T> {

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUserManager.getInstance().setSignInOtherDeviceListener(() -> {
            AlertDialog.Builder builder = new Builder(this);
            builder.setTitle("当前用户已经被挤下线");
            builder.setMessage("当前用户已经被挤下线");
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
        });
    }
}
