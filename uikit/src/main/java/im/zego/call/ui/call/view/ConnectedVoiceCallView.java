package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Objects;

import im.zego.call.R;
import im.zego.call.constant.Constants;
import im.zego.call.databinding.LayoutConnectedVoiceCallBinding;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.AudioHelper;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.listener.ZegoDeviceServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoCallService;
import im.zego.callsdk.service.ZegoDeviceService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.callsdk.service.ZegoStreamService;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zegoexpress.constants.ZegoAudioRoute;

public class ConnectedVoiceCallView extends ConstraintLayout {

    private LayoutConnectedVoiceCallBinding binding;
    private ZegoUserInfo userInfo;

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
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        binding.callVoiceHangUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callService.endCall(errorCode -> {
                    if (errorCode == 0) {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_COMPLETED);
                    } else {
                        ToastUtils.showShort(R.string.end_call_failed, errorCode);
                    }
                });
            }
        });
        ZegoUserInfo localUserInfo = userService.getLocalUserInfo();
        binding.callVoiceMic.setSelected(localUserInfo.mic);
        binding.callVoiceMic.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.muteMic(!selected);
        });
        binding.callVoiceSpeaker.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.enableSpeaker(!selected);
        });
        binding.callVoiceMinimal.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_MINIMAL, Boolean.class).post(true);
        });
        binding.callVoiceSettings.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_SHOW_SETTINGS, Boolean.class).post(false);
        });
        AudioHelper.updateAudioSelect(binding.callVoiceSpeaker,
            ZegoServiceManager.getInstance().deviceService.getAudioRouteType());
        ZegoServiceManager.getInstance().deviceService.setListener(new ZegoDeviceServiceListener() {
            @Override
            public void onAudioRouteChange(ZegoAudioRoute audioRoute) {
                AudioHelper.updateAudioSelect(binding.callVoiceSpeaker, audioRoute);
            }
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            ZegoStreamService streamService = ZegoServiceManager.getInstance().streamService;
            if (visibility == View.VISIBLE) {
                streamService.startPlaying(userInfo.userID, null);
            }
        }
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (Objects.equals(userService.getLocalUserInfo(), userInfo)) {
            binding.callVoiceMic.setSelected(userInfo.mic);
        }
    }
}
