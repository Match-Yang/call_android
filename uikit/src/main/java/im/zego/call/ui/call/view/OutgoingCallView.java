package im.zego.call.ui.call.view;

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

import im.zego.callsdk.service.ZegoCallService;
import im.zego.callsdk.service.ZegoServiceManager;
import java.util.Objects;

import im.zego.call.R;
import im.zego.call.constant.Constants;
import im.zego.call.databinding.LayoutOutgoingCallBinding;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoCancelType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoUserService;

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
        ZegoUserService userService = ZegoServiceManager.getInstance().userService;
        ZegoCallService callService = ZegoServiceManager.getInstance().callService;
        binding.callingHangUp.setOnClickListener(v -> {
            callService.cancelCall(userInfo.userID, errorCode -> {
                if (errorCode == 0) {
                    binding.callStateText.setText(R.string.call_page_status_canceled);
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_CANCELED);
                } else {
                    ToastUtils.showShort(R.string.cancel_call_failed, errorCode);
                }
            });
        });
        //        binding.cameraSwitch.setSelected(true);
        //        binding.cameraSwitch.setOnClickListener(new OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                boolean selected = v.isSelected();
        //                v.setSelected(!selected);
        //                userService.useFrontCamera(selected);
        //            }
        //        });
        //        binding.callMinimal.setOnClickListener(v -> {
        //            LiveEventBus.get(Constants.EVENT_MINIMAL, Boolean.class).post(true);
        //        });
        //        binding.callSettings.setOnClickListener(v -> {
        //            LiveEventBus.get(Constants.EVENT_SHOW_SETTINGS, Boolean.class).post(isVideoCall());
        //        });
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
}
