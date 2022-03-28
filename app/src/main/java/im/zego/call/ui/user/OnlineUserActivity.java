package im.zego.call.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.scwang.smart.refresh.header.MaterialHeader;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.service.ZegoCallService;
import java.util.List;

import im.zego.call.R;
import im.zego.call.ZegoCallKit;
import im.zego.call.databinding.ActivityOnlineUserBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.OnRecyclerViewItemTouchListener;
import im.zego.callsdk.callback.ZegoRoomCallback;
import im.zego.callsdk.listener.ZegoUserListCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoUserService;

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
                    return;
                }
                if (itemChild.getId() == R.id.item_online_user_voice) {
                    ZegoCallService callService = ZegoServiceManager.getInstance().callService;
                    callService.callUser(userInfo.userID, ZegoCallType.Voice, null, new ZegoCallback() {
                        @Override
                        public void onResult(int errorCode) {
                            Log.d("callUser", "onResult() called with: errorCode = [" + errorCode + "]");
                            if (errorCode == 0) {
                                ZegoCallKit.getInstance()
                                    .callUser(userInfo, CallStateManager.TYPE_OUTGOING_CALLING_VOICE);
                            }
                        }
                    });

                } else if (itemChild.getId() == R.id.item_online_user_video) {
                    ZegoCallService callService = ZegoServiceManager.getInstance().callService;
                    callService.callUser(userInfo.userID, ZegoCallType.Video, null, new ZegoCallback() {
                        @Override
                        public void onResult(int errorCode) {
                            Log.d("callUser", "onResult() called with: errorCode = [" + errorCode + "]");
                            if (errorCode == 0) {
                                ZegoCallKit.getInstance()
                                    .callUser(userInfo, CallStateManager.TYPE_OUTGOING_CALLING_VIDEO);
                            }
                        }
                    });
                }
            }
        });
    }

    private void getUserList(ZegoRoomCallback callback) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        userService.getOnlineUserList(new ZegoUserListCallback() {
            @Override
            public void onGetUserList(int errorCode, List<ZegoUserInfo> userInfoList) {
                userInfoList.remove(userService.localUserInfo);
                onlineUserAdapter.updateList(userInfoList);
                callback.onRoomCallback(errorCode);
            }
        });
    }
}