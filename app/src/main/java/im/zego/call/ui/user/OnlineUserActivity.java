package im.zego.call.ui.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blankj.utilcode.util.ActivityUtils;
import com.scwang.smart.refresh.header.MaterialHeader;
import im.zego.call.R;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.http.WebClientManager;
import im.zego.call.http.bean.UserBean;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.utils.OnRecyclerViewItemTouchListener;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnlineUserActivity extends BaseActivity<ActivityOnlineUserBinding> {

    private OnlineUserAdapter onlineUserAdapter;
    private ReceiveCallDialog callDialog;

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
        binding.smartRefreshLayout.setRefreshHeader(new MaterialHeader(this));
        binding.smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            getUserList(errorCode -> {
                refreshLayout.finishRefresh();
            });
        });
        getUserList(errorCode -> {

        });
        binding.userRecyclerview.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(binding.userRecyclerview) {
            @Override
            public void onItemChildClick(ViewHolder vh, View itemChild) {
                super.onItemChildClick(vh, itemChild);
                int adapterPosition = vh.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                ZegoUserInfo userInfo = onlineUserAdapter.getUserInfo(adapterPosition);
                if (itemChild.getId() == R.id.item_online_user_voice) {
                    CallActivity.startCallActivity(CallActivity.TYPE_OUTGOING_CALLING_VOICE, userInfo);

                } else if (itemChild.getId() == R.id.item_online_user_video) {
                    CallActivity.startCallActivity(CallActivity.TYPE_OUTGOING_CALLING_VIDEO, userInfo);
                }
            }
        });
    }

    private void getUserList(ZegoRoomCallback callback) {
        WebClientManager.getInstance().getUserList((errorCode, message, response) -> {
            if (callback != null) {
                callback.onRoomCallback(errorCode);
            }
            List<ZegoUserInfo> userInfoList = new ArrayList<>();
            ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
            for (UserBean userBean : response) {
                if (Objects.equals(userBean.userID, localUserInfo.userID)) {
                    continue;
                }
                ZegoUserInfo userInfo = new ZegoUserInfo();
                userInfo.userID = userBean.userID;
                userInfo.userName = userBean.userName;
                userInfoList.add(userInfo);
            }
            onlineUserAdapter.updateList(userInfoList);
        });
    }
}