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

import java.util.Objects;

import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.databinding.LayoutIncomingCallBinding;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoDeclineType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.interfaces.ZegoDeviceService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.core.interfaces.ZegoUserService;
import im.zego.zim.enums.ZIMErrorCode;

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
                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                ZegoCallService callService = ZegoServiceManager.getInstance().callService;
                ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;

                String token = AuthInfoManager.getInstance().generateToken(userService.getLocalUserInfo().userID);
//                String token = TokenManager.getInstance().tokenWrapper.token;
                callService.acceptCall(token, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        deviceService.enableMic(true);
                        deviceService.enableCamera(true);
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CONNECTED_VIDEO);
                    } else {
                        ToastUtils.showShort(R.string.response_failed, errorCode);
                    }
                });
            }
        });
        binding.callAcceptVoice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUserService userService = ZegoServiceManager.getInstance().userService;
                ZegoCallService callService = ZegoServiceManager.getInstance().callService;
                ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;

                String token = AuthInfoManager.getInstance().generateToken(userService.getLocalUserInfo().userID);
//                String token = TokenManager.getInstance().tokenWrapper.token;
                callService.acceptCall(token, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        deviceService.enableMic(true);
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CONNECTED_VOICE);
                    } else {
                        ToastUtils.showShort("responseCall " + errorCode);
                    }
                });
            }
        });
        binding.callDecline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoCallService callService = ZegoServiceManager.getInstance().callService;
                callService.declineCall(userInfo.userID, ZegoDeclineType.Decline, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                    } else {
                        ToastUtils.showShort("Decline Call" + errorCode);
                    }
                });
            }
        });
    }

    public void setCallType(int callType) {
        boolean isVideoCall = callType == CallStateManager.TYPE_INCOMING_CALLING_VIDEO;
        boolean isAudioCall = callType == CallStateManager.TYPE_INCOMING_CALLING_VOICE;
        if (isVideoCall) {
            binding.callAcceptVoice.setVisibility(GONE);
            binding.callAcceptVideo.setVisibility(VISIBLE);
        } else if (isAudioCall) {
            binding.callAcceptVoice.setVisibility(VISIBLE);
            binding.callAcceptVideo.setVisibility(GONE);
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
        if (Objects.equals(this.userInfo, userInfo)) {
            setUserInfo(userInfo);
        }
    }

    public void updateStateText(int stringID) {
        binding.callStateText.setText(stringID);
    }
}
