package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.databinding.LayoutIncomingCallBinding;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;

public class IncomingCallView extends ConstraintLayout {

    private LayoutIncomingCallBinding binding;
    private ZegoUserInfo userInfo;

    public IncomingCallView(@NonNull Context context) {
        super(context);
        initView();
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LayoutIncomingCallBinding.inflate(LayoutInflater.from(getContext()), this);
        binding.callAcceptVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.callAcceptVoice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.callDecline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.callCameraSwitchSmall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void updateUi(boolean isVideoCall) {
        binding.callAcceptVoice.setVisibility(isVideoCall ? GONE : VISIBLE);
        binding.callAcceptVideo.setVisibility(isVideoCall ? VISIBLE : GONE);
        binding.callCameraSwitchSmall.setVisibility(isVideoCall ? VISIBLE : GONE);
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public void onLocalUserChanged(ZegoUserInfo localUserInfo) {

    }
}
