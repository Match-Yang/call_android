package im.zego.call.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.gyf.immersionbar.ImmersionBar;
import im.zego.call.databinding.ActivityLoginBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.entry.EntryActivity;
import java.util.Random;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

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
                ActivityUtils.startActivity(EntryActivity.class);
            }
        });
        int nextInt = Math.abs(new Random().nextInt());
        String manufacturer = DeviceUtils.getManufacturer();
        binding.loginUsername.setText(manufacturer + nextInt);
    }
}