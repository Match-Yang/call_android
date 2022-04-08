package im.zego.call.ui.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.blankj.utilcode.util.ToastUtils;
import com.scwang.smart.refresh.header.MaterialHeader;

import java.util.List;

import im.zego.call.R;
import im.zego.call.UIKitActivity;
import im.zego.call.firebase.FirebaseUserManager;
import im.zego.calluikit.ZegoCallManager;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.utils.OnRecyclerViewItemTouchListener;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.ui.BaseActivity;
import im.zego.calluikit.ui.call.CallStateManager;

public class OnlineUserActivity extends UIKitActivity<ActivityOnlineUserBinding> {

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.userRecyclerview.setLayoutManager(layoutManager);
        onlineUserAdapter = new OnlineUserAdapter(null);
        binding.userRecyclerview.setAdapter(onlineUserAdapter);
        binding.smartRefreshLayout.setRefreshHeader(new MaterialHeader(this));
        binding.smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            getUserList(errorCode -> {
                if (errorCode != 0) {
                    showWarnTips(getString(R.string.get_user_list_failed, errorCode));
                }
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
                boolean inACallStream = CallStateManager.getInstance().isInACallStream();
                if (inACallStream) {
                    ToastUtils.showShort("On the line. Unable to initiate a new call.");
                    return;
                }
                if (itemChild.getId() == R.id.item_online_user_voice) {
                    ZegoCallManager.getInstance()
                        .callUser(userInfo, CallStateManager.TYPE_OUTGOING_CALLING_VOICE);
                } else if (itemChild.getId() == R.id.item_online_user_video) {
                    ZegoCallManager.getInstance()
                        .callUser(userInfo, CallStateManager.TYPE_OUTGOING_CALLING_VIDEO);
                }
            }
        });
    }

    private void getUserList(ZegoCallback callback) {
        List<ZegoUserInfo> onlineUserList = FirebaseUserManager.getInstance().getOnlineUserList();
        onlineUserAdapter.updateList(onlineUserList);
        if (callback != null) {
            callback.onResult(0);
        }
    }
}