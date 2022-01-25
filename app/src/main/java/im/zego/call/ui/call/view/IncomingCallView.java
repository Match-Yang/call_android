package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.databinding.LayoutIncomingCallBinding;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.Objects;

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
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                String token = AuthInfoManager.getInstance().generateJoinRoomToken(userService.localUserInfo.userID);
                userService.responseCall(ZegoResponseType.Accept, userInfo.userID, token, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        userService.micOperate(true, errorCode1 -> {
                            if (errorCode1 == 0) {
                                userService.cameraOperate(true, errorCode2 -> {
                                    if (errorCode2 == 0) {
                                    }
                                });
                            }
                        });
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CONNECTED_VIDEO);
                    } else {
                        ToastUtils.showShort("responseCall " + errorCode);
                    }
                });
            }
        });
        binding.callAcceptVoice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                String token = AuthInfoManager.getInstance().generateJoinRoomToken(userService.localUserInfo.userID);
                userService.responseCall(ZegoResponseType.Accept, userInfo.userID, token, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        userService.micOperate(true, errorCode1 -> {
                            if (errorCode1 == 0) {
                            } else {
                                ToastUtils.showShort(getContext().getString(R.string.mic_operate_failed, errorCode1));
                            }
                        });
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
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                userService.responseCall(ZegoResponseType.Decline, userInfo.userID, null, errorCode -> {
                    if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                    } else {
                        ToastUtils.showShort("Decline Call" + errorCode);
                    }
                });
            }
        });
        binding.callCameraSwitchSmall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                userService.useFrontCamera(false);
            }
        });
    }

    public void updateUi(boolean isVideoCall) {
        binding.callAcceptVoice.setVisibility(isVideoCall ? GONE : VISIBLE);
        binding.callAcceptVideo.setVisibility(isVideoCall ? VISIBLE : GONE);
        binding.callCameraSwitchSmall.setVisibility(isVideoCall ? VISIBLE : GONE);
        binding.callUserTexture.setVisibility(isVideoCall ? VISIBLE : GONE);
        if (isVideoCall) {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            userService.startPlayingUserMedia(userService.localUserInfo.userID, binding.callUserTexture);
        }
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
        Drawable fullDrawable = AvatarHelper.getFullAvatarByUserName(userName);
        binding.callUserBg.setImageDrawable(fullDrawable);
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
