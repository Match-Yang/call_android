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
import im.zego.call.utils.AudioHelper;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.listener.ZegoDeviceServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoServiceManager;
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
//        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
//        binding.callVoiceHangUp.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                userService.endCall(errorCode -> {
//                    if (errorCode == 0) {
//                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_COMPLETED);
//                    } else {
//                        ToastUtils.showShort(R.string.end_call_failed, errorCode);
//                    }
//                });
//            }
//        });
//        ZegoUserInfo localUserInfo = userService.localUserInfo;
//        binding.callVoiceMic.setSelected(localUserInfo.mic);
//        binding.callVoiceMic.setOnClickListener(v -> {
//            boolean selected = v.isSelected();
//            userService.enableMic(!selected, errorCode -> {
//                if (errorCode == 0) {
//                    v.setSelected(!selected);
//                }else {
//                    ToastUtils.showShort(R.string.camera_operate_failed,errorCode);
//                }
//            });
//        });
//        binding.callVoiceSpeaker.setOnClickListener(v -> {
//            boolean selected = v.isSelected();
//            v.setSelected(!selected);
//            userService.speakerOperate(!selected);
//        });
//        binding.callVoiceMinimal.setOnClickListener(v -> {
//            LiveEventBus.get(Constants.EVENT_MINIMAL, Boolean.class).post(true);
//        });
//        binding.callVoiceSettings.setOnClickListener(v -> {
//            LiveEventBus.get(Constants.EVENT_SHOW_SETTINGS, Boolean.class).post(false);
//        });
        AudioHelper.updateAudioSelect(binding.callVoiceSpeaker, ZegoServiceManager.getInstance().deviceService.getAudioRouteType());
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
//        if (changedView == this) {
//            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
//            if (visibility == View.VISIBLE) {
//                userService.startPlaying(userInfo.userID, null);
//            }
//        }
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
//        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
//        if (Objects.equals(userService.localUserInfo, userInfo)) {
//            binding.callVoiceMic.setSelected(userInfo.mic);
//        }
    }
}
