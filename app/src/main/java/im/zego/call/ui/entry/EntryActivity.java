package im.zego.call.ui.entry;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.call.databinding.ActivityEntryBinding;
import im.zego.call.http.WebClientManager;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.common.ReceiveCallDialog;
import im.zego.call.ui.login.LoginActivity;
import im.zego.call.ui.setting.SettingActivity;
import im.zego.call.ui.user.OnlineUserActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class EntryActivity extends BaseActivity<ActivityEntryBinding> {

    public static final String URL_GET_MORE = "https://www.zegocloud.com/";
    public static final String URL_CONTACT_US = "https://www.zegocloud.com/talk";
    private ReceiveCallDialog callDialog;
    private static final String TAG = "EntryActivity";
    private ZegoUserServiceListener userServiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.entrySetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(SettingActivity.class);
            }
        });
        binding.entryContactUs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_CONTACT_US);
            }
        });
        binding.entryGetMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.startWebViewActivity(URL_GET_MORE);
            }
        });
        binding.entryBannerCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(OnlineUserActivity.class);
            }
        });

        userServiceListener = new ZegoUserServiceListener() {
            @Override
            public void onUserInfoUpdated(ZegoUserInfo userInfo) {

            }

            @Override
            public void onCallReceived(ZegoUserInfo userInfo, ZegoCallType type) {
                Log.d(TAG, "onCallReceived() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                callDialog = new ReceiveCallDialog(ActivityUtils.getTopActivity(), userInfo, type);
                if (!callDialog.isShowing()) {
                    callDialog.show();
                }
            }

            @Override
            public void onCancelCallReceived(ZegoUserInfo userInfo) {
                if (callDialog != null) {
                    callDialog.dismiss();
                }
            }

            @Override
            public void onCallResponseReceived(ZegoUserInfo userInfo, ZegoResponseType type) {

            }

            @Override
            public void onEndCallReceived() {

            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {

            }
        };
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(userServiceListener);
        ZegoUserInfo localUserInfo = userService.localUserInfo;

        binding.entryUserId.setText(localUserInfo.userID);
        binding.entryUserName.setText(localUserInfo.userName);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(localUserInfo.userName);
        binding.entryUserAvatar.setImageDrawable(userIcon);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(userServiceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.logout();
        WebClientManager.getInstance().stopHeartBeat();
        ActivityUtils.finishToActivity(LoginActivity.class, false);
    }
}