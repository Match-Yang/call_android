package im.zego.call.ui.entry;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.call.databinding.ActivityEntryBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.setting.SettingActivity;
import im.zego.call.ui.user.OnlineUserActivity;
import im.zego.call.ui.webview.WebViewActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;

public class EntryActivity extends BaseActivity<ActivityEntryBinding> {

    public static final String URL_GET_MORE = "https://www.zegocloud.com/";
    public static final String URL_CONTACT_US = "https://www.zegocloud.com/talk";

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

        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;

        binding.entryUserId.setText(localUserInfo.userID);
        binding.entryUserName.setText(localUserInfo.userName);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(localUserInfo.userName);
        binding.entryUserAvatar.setImageDrawable(userIcon);
    }
}