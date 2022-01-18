package im.zego.call.ui.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.http.IAsyncGetCallback;
import im.zego.call.http.WebClientManager;
import im.zego.call.http.bean.UserBean;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.common.OnCallDialog;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import java.util.List;

public class OnlineUserActivity extends BaseActivity<ActivityOnlineUserBinding> {

    private OnlineUserAdapter onlineUserAdapter;

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
        onlineUserAdapter = new OnlineUserAdapter(null);
        binding.userRecyclerview.setAdapter(onlineUserAdapter);
        WebClientManager.getInstance().getUserList((errorCode, message, response) -> {
            onlineUserAdapter.updateList(response);
        });
    }
}