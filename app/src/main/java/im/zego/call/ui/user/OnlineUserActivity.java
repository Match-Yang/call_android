package im.zego.call.ui.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.common.OnCallDialog;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;

public class OnlineUserActivity extends BaseActivity<ActivityOnlineUserBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.userBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //                onBackPressed();
                OnCallDialog dialog = new OnCallDialog(OnlineUserActivity.this, ZegoCallType.Audio);
                dialog.show();
            }
        });

        binding.userRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        binding.userRecyclerview.setAdapter(new OnlineUserAdapter(userService.getUserList()));
    }
}