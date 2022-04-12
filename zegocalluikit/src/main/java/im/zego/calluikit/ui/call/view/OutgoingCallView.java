package im.zego.calluikit.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Objects;

import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoDeviceService;
import im.zego.callsdk.core.interfaces.ZegoStreamService;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.R;
import im.zego.calluikit.constant.Constants;
import im.zego.calluikit.databinding.LayoutOutgoingCallBinding;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.ui.common.MinimalView;
import im.zego.calluikit.utils.AvatarHelper;

public class OutgoingCallView extends ConstraintLayout {

    private LayoutOutgoingCallBinding binding;
    private ZegoUserInfo userInfo;
    private int typeOfCall;

    public OutgoingCallView(@NonNull Context context) {
        super(context);
        initView();
    }

    public OutgoingCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OutgoingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public OutgoingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LayoutOutgoingCallBinding.inflate(LayoutInflater.from(getContext()), this);
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;

        binding.callingHangUp.setOnClickListener(v -> {
            callService.cancelCall(errorCode -> {
                if (errorCode == 0) {
                    binding.callStateText.setText(R.string.call_page_status_canceled);
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                } else {
                    ToastUtils.showShort(R.string.cancel_call_failed, errorCode);
                }
            });
        });
        binding.cameraSwitch.setSelected(true);
        binding.cameraSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                deviceService.useFrontCamera(selected);
            }
        });
        binding.callMinimal.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_MINIMAL, Boolean.class).post(true);
        });
        binding.callSettings.setOnClickListener(v -> {
            LiveEventBus.get(Constants.EVENT_SHOW_SETTINGS, Boolean.class).post(isVideoCall());
        });
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public void setCallType(int typeOfCall) {
        this.typeOfCall = typeOfCall;
        if (isVideoCall()) {
            binding.textureView.setVisibility(View.VISIBLE);
            binding.cameraSwitch.setVisibility(View.VISIBLE);
        } else if (isAudioCall()) {
            binding.textureView.setVisibility(View.GONE);
            binding.cameraSwitch.setVisibility(View.GONE);
        }
    }

    public void updateStateText(@StringRes int stateText) {
        binding.callStateText.setText(stateText);
    }

    private boolean isVideoCall() {
        return typeOfCall == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO;
    }

    private boolean isAudioCall() {
        return typeOfCall == CallStateManager.TYPE_OUTGOING_CALLING_VOICE;
    }

    public TextureView getTextureView() {
        return binding.textureView;
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        if (Objects.equals(this.userInfo, userInfo)) {
            setUserInfo(userInfo);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoStreamService streamService = ZegoServiceManager.getInstance().streamService;
        if (getVisibility() == View.VISIBLE && !MinimalView.isShowMinimal) {
            streamService.startPlaying(userService.getLocalUserInfo().userID, binding.textureView);
        }
    }
}
