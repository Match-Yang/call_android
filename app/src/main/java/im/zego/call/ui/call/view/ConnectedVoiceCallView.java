package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.databinding.LayoutConnectedVoiceCallBinding;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;

public class ConnectedVoiceCallView extends ConstraintLayout {

    private LayoutConnectedVoiceCallBinding binding;
    private ZegoUserInfo userInfo;
    private ConnectedVoiceCallLister listener;

    public ConnectedVoiceCallView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ConnectedVoiceCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ConnectedVoiceCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ConnectedVoiceCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LayoutConnectedVoiceCallBinding.inflate(LayoutInflater.from(getContext()), this);
        binding.callVoiceHangUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                userService.endCall(errorCode -> {
                    if (listener != null) {
                        listener.onEndCall(errorCode);
                    }
                });
            }
        });
    }

    public void setListener(ConnectedVoiceCallLister listener) {
        this.listener = listener;
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public interface ConnectedVoiceCallLister {

        void onEndCall(int errorCode);
    }
}
