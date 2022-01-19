package im.zego.call.ui.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import im.zego.call.R;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.http.WebClientManager;
import im.zego.call.http.bean.UserBean;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.utils.OnRecyclerViewItemTouchListener;
import im.zego.callsdk.model.ZegoUserInfo;

public class OnlineUserActivity extends BaseActivity<ActivityOnlineUserBinding> {

    private OnlineUserAdapter onlineUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.userBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.userRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        onlineUserAdapter = new OnlineUserAdapter(null);
        binding.userRecyclerview.setAdapter(onlineUserAdapter);
        WebClientManager.getInstance().getUserList((errorCode, message, response) -> {
            onlineUserAdapter.updateList(response);
        });
        binding.userRecyclerview.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(binding.userRecyclerview) {
            @Override
            public void onItemChildClick(ViewHolder vh, View itemChild) {
                super.onItemChildClick(vh, itemChild);
                int adapterPosition = vh.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                UserBean userBean = onlineUserAdapter.getUserBean(adapterPosition);
                ZegoUserInfo userInfo = new ZegoUserInfo();
                userInfo.userID = userBean.userID;
                userInfo.userName = userBean.userName;
                if (itemChild.getId() == R.id.item_online_user_voice) {
                    CallActivity.startCallActivity(CallActivity.TYPE_OUTGOING_CALLING_VOICE, userInfo);

                } else if (itemChild.getId() == R.id.item_online_user_video) {
                    CallActivity.startCallActivity(CallActivity.TYPE_OUTGOING_CALLING_VIDEO, userInfo);
                }
            }
        });
    }
}