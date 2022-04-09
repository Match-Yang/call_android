package im.zego.calluikit.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Objects;

import im.zego.calluikit.R;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.databinding.LayoutConnectedVideoCallBinding;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.ui.common.MinimalView;
import im.zego.calluikit.utils.AudioHelper;
import im.zego.calluikit.utils.AvatarHelper;
import im.zego.callsdk.listener.ZegoDeviceServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoDeviceService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.core.interfaces.ZegoStreamService;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.zegoexpress.constants.ZegoAudioRoute;

public class ConnectedVideoCallView extends ConstraintLayout {

    private LayoutConnectedVideoCallBinding binding;
    private ZegoUserInfo userInfo;
    private boolean isSelfCenter = true;

    public ConnectedVideoCallView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ConnectedVideoCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ConnectedVideoCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ConnectedVideoCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LayoutConnectedVideoCallBinding.inflate(LayoutInflater.from(getContext()), this);
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;
        ZegoStreamService streamService = ZegoServiceManager.getInstance().streamService;
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        binding.callVideoHangUp.setOnClickListener(new OnClickListener() {
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
        binding.callVideoCamera.setSelected(localUserInfo.camera);
        binding.callVideoCamera.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.enableCamera(!selected);
        });
        binding.callVideoMic.setSelected(localUserInfo.mic);
        binding.callVideoMic.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.enableMic(!selected);
        });
        binding.callVideoCameraSwitch.setSelected(true);
        binding.callVideoCameraSwitch.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.useFrontCamera(!selected);
        });
        binding.callVideoSpeaker.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            deviceService.enableSpeaker(!selected);
        });
        binding.callVideoViewSmallLayout.setOnClickListener(v -> {
            isSelfCenter = !isSelfCenter;
            if (isSelfCenter) {
                binding.callVideoViewSmallName.setText(userInfo.userName);
                streamService.startPlaying(userService.getLocalUserInfo().userID, binding.callVideoViewCenterTexture);
                streamService.startPlaying(userInfo.userID, binding.callVideoViewSmallTexture);
            } else {
                binding.callVideoViewSmallName.setText(R.string.me);
                streamService.startPlaying(userService.getLocalUserInfo().userID, binding.callVideoViewSmallTexture);
                streamService.startPlaying(userInfo.userID, binding.callVideoViewCenterTexture);
            }
            onUserInfoUpdated(userInfo);
            onUserInfoUpdated(localUserInfo);
        });
        binding.callVideoMinimal.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_MINIMAL, Boolean.class).post(true);
        });
        binding.callVideoSettings.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_SHOW_SETTINGS, Boolean.class).post(true);
        });
        ZegoServiceManager.getInstance().deviceService.addListener(new ZegoDeviceServiceListener() {
            @Override
            public void onAudioRouteChange(ZegoAudioRoute audioRoute) {
                AudioHelper.updateAudioSelect(binding.callVideoSpeaker, audioRoute);
            }
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoStreamService streamService = ZegoServiceManager.getInstance().streamService;
        if (visibility == View.VISIBLE && !MinimalView.isShowMinimal) {
            AudioHelper.updateAudioSelect(binding.callVideoSpeaker, ZegoServiceManager.getInstance().deviceService.getAudioRouteType());
            if (isSelfCenter) {
                streamService.startPlaying(userService.getLocalUserInfo().userID, binding.callVideoViewCenterTexture);
                streamService.startPlaying(userInfo.userID, binding.callVideoViewSmallTexture);
            } else {
                streamService.startPlaying(userService.getLocalUserInfo().userID, binding.callVideoViewSmallTexture);
                streamService.startPlaying(userInfo.userID, binding.callVideoViewCenterTexture);
            }
        }
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        binding.callVideoViewSmallName.setText(userInfo.userName);
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        Log.d("userInfo", "onUserInfoUpdated() called with: userInfo = [" + userInfo + "]");
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        if (Objects.equals(userService.getLocalUserInfo(), userInfo)) {
            binding.callVideoMic.setSelected(userInfo.mic);
            binding.callVideoCamera.setSelected(userInfo.camera);
            if (isSelfCenter) {
                if (userInfo.camera) {
                    binding.callVideoViewCenterIcon.setVisibility(View.GONE);
                } else {
                    Drawable fullAvatar = AvatarHelper.getFullAvatarByUserName(userInfo.userName);
                    binding.callVideoViewCenterIcon.setImageDrawable(fullAvatar);
                    binding.callVideoViewCenterIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (userInfo.camera) {
                    binding.callVideoViewSmallIcon.setVisibility(View.GONE);
                } else {
                    Drawable fullAvatar = AvatarHelper.getFullAvatarByUserName(userInfo.userName);
                    binding.callVideoViewSmallIcon.setImageDrawable(fullAvatar);
                    binding.callVideoViewSmallIcon.setVisibility(View.VISIBLE);
                }
            }
        } else if (Objects.equals(this.userInfo, userInfo)) {
            this.userInfo = userInfo;
            if (isSelfCenter) {
                if (userInfo.camera) {
                    binding.callVideoViewSmallIcon.setVisibility(View.GONE);
                } else {
                    Drawable fullAvatar = AvatarHelper.getFullAvatarByUserName(userInfo.userName);
                    binding.callVideoViewSmallIcon.setImageDrawable(fullAvatar);
                    binding.callVideoViewSmallIcon.setVisibility(View.VISIBLE);
                }
            } else {
                if (userInfo.camera) {
                    binding.callVideoViewCenterIcon.setVisibility(View.GONE);
                } else {
                    Drawable fullAvatar = AvatarHelper.getFullAvatarByUserName(userInfo.userName);
                    binding.callVideoViewCenterIcon.setImageDrawable(fullAvatar);
                    binding.callVideoViewCenterIcon.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
