package im.zego.call.ui.call.view;

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
import im.zego.call.R;
import im.zego.call.databinding.LayoutConnectedVideoCallBinding;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import java.util.Objects;

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
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        binding = LayoutConnectedVideoCallBinding.inflate(LayoutInflater.from(getContext()), this);

        binding.callVideoHangUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                userService.endCall(errorCode -> {
                    if (errorCode == 0) {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_COMPLETED);
                    } else {
                        ToastUtils.showShort(R.string.end_call_failed, errorCode);
                    }
                });
            }
        });
        ZegoUserInfo localUserInfo = userService.localUserInfo;
        binding.callVideoCamera.setSelected(localUserInfo.camera);
        binding.callVideoCamera.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            userService.enableCamera(!selected, errorCode -> {
                if (errorCode == 0) {
                    v.setSelected(!selected);
                } else {
                    ToastUtils.showShort(R.string.camera_operate_failed, errorCode);
                }
            });
        });
        binding.callVideoMic.setSelected(localUserInfo.mic);
        binding.callVideoMic.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            userService.enableMic(!selected, errorCode -> {
                if (errorCode == 0) {
                    v.setSelected(!selected);
                } else {
                    ToastUtils.showShort(R.string.mic_operate_failed, errorCode);
                }
            });
        });
        binding.callVideoCameraSwitch.setSelected(true);
        binding.callVideoCameraSwitch.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            userService.useFrontCamera(!selected);
        });
        binding.callVideoSpeaker.setSelected(true);
        binding.callVideoSpeaker.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            userService.speakerOperate(!selected);
        });
        binding.callVideoViewSmallLayout.setOnClickListener(v -> {
            isSelfCenter = !isSelfCenter;
            if (isSelfCenter) {
                binding.callVideoViewSmallName.setText(userInfo.userName);
                userService.startPlaying(userService.localUserInfo.userID, binding.callVideoViewCenterTexture);
                userService.startPlaying(userInfo.userID, binding.callVideoViewSmallTexture);
            } else {
                binding.callVideoViewSmallName.setText(R.string.me);
                userService.startPlaying(userService.localUserInfo.userID, binding.callVideoViewSmallTexture);
                userService.startPlaying(userInfo.userID, binding.callVideoViewCenterTexture);
            }
            onUserInfoUpdated(userInfo);
            onUserInfoUpdated(localUserInfo);
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            if (visibility == View.VISIBLE) {
                if (isSelfCenter) {
                    userService
                        .startPlaying(userService.localUserInfo.userID, binding.callVideoViewCenterTexture);
                    userService.startPlaying(userInfo.userID, binding.callVideoViewSmallTexture);
                } else {
                    userService
                        .startPlaying(userService.localUserInfo.userID, binding.callVideoViewSmallTexture);
                    userService.startPlaying(userInfo.userID, binding.callVideoViewCenterTexture);
                }
            }
        }
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        binding.callVideoViewSmallName.setText(userInfo.userName);
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        Log.d("userInfo", "onUserInfoUpdated() called with: userInfo = [" + userInfo + "]");
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        if (Objects.equals(userService.localUserInfo, userInfo)) {
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
