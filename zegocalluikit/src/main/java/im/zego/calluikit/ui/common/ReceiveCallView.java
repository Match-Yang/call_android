package im.zego.calluikit.ui.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;

import im.zego.callsdk.core.interfaces.ZegoCallService;
import im.zego.callsdk.core.manager.ZegoServiceManager;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoErrorCode;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.calluikit.R;
import im.zego.calluikit.databinding.LayoutReceiveCallBinding;
import im.zego.calluikit.ui.call.CallActivity;
import im.zego.calluikit.ui.call.CallStateManager;
import im.zego.calluikit.utils.AvatarHelper;
import im.zego.calluikit.utils.TokenManager;

public class ReceiveCallView extends FrameLayout {

    private LayoutReceiveCallBinding binding;
    private OnReceiveCallViewClickedListener listener;
    private ZegoUserInfo userInfo;
    private ZegoCallType callType = ZegoCallType.Voice;

    public ReceiveCallView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = LayoutReceiveCallBinding.inflate(LayoutInflater.from(context), this, true);
        if (callType == ZegoCallType.Voice) {
            binding.dialogCallAcceptVoice.setVisibility(View.VISIBLE);
            binding.dialogCallAcceptVideo.setVisibility(View.GONE);
        } else {
            binding.dialogCallAcceptVoice.setVisibility(View.GONE);
            binding.dialogCallAcceptVideo.setVisibility(View.VISIBLE);
        }
        if (userInfo != null) {
            binding.dialogCallName.setText(userInfo.userName);
            Drawable userIcon = AvatarHelper.getAvatarByUserName(userInfo.userName);
            binding.dialogCallIcon.setImageDrawable(userIcon);
        }

        if (callType == ZegoCallType.Voice) {
            binding.dialogCallType.setText(R.string.zego_voice_call);
        } else {
            binding.dialogCallType.setText(R.string.zego_video_call);
        }

        binding.dialogCallAcceptVoice.setOnClickListener(v -> {
            ZegoCallService callService = ZegoServiceManager.getInstance().callService;
            String token = TokenManager.getInstance().getTokenWrapper().token;
            callService.acceptCall(token, errorCode -> {
                if (errorCode == ZegoErrorCode.SUCCESS) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CONNECTED_VOICE);
                    CallActivity.startCallActivity(userInfo);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_COMPLETED);
                    ToastUtils.showShort("responseCall " + errorCode);
                }
                if (listener != null) {
                    listener.onAcceptAudioClicked();
                }
            });
        });
        binding.dialogCallAcceptVideo.setOnClickListener(v -> {
            ZegoCallService callService = ZegoServiceManager.getInstance().callService;
            String token = TokenManager.getInstance().getTokenWrapper().token;
            callService.acceptCall(token, errorCode -> {
                if (errorCode == ZegoErrorCode.SUCCESS) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CONNECTED_VIDEO);
                    CallActivity.startCallActivity(userInfo);
                } else {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_COMPLETED);
                    ToastUtils.showShort("responseCall " + errorCode);
                }
                if (listener != null) {
                    listener.onAcceptVideoClicked();
                }
            });
        });
        binding.dialogCallDecline.setOnClickListener(v -> {
            ZegoCallService callService = ZegoServiceManager.getInstance().callService;
            callService.declineCall(errorCode -> {
                if (errorCode == ZegoErrorCode.SUCCESS) {
                    CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_DECLINE);
                } else {
                    ToastUtils.showShort("Decline Call" + errorCode);
                }
                if (listener != null) {
                    listener.onDeclineClicked();
                }
            });
        });
        binding.dialogReceiveCall.setOnClickListener(v -> {
            CallActivity.startCallActivity(userInfo);
            if (listener != null) {
                listener.onWindowClicked();
            }
        });
    }

    public void updateData(ZegoUserInfo userInfo, ZegoCallType callType) {
        this.userInfo = userInfo;
        this.callType = callType;
        if (callType == ZegoCallType.Voice) {
            binding.dialogCallAcceptVoice.setVisibility(View.VISIBLE);
            binding.dialogCallAcceptVideo.setVisibility(View.GONE);
        } else {
            binding.dialogCallAcceptVoice.setVisibility(View.GONE);
            binding.dialogCallAcceptVideo.setVisibility(View.VISIBLE);
        }
        if (userInfo != null) {
            binding.dialogCallName.setText(userInfo.userName);
            Drawable userIcon = AvatarHelper.getAvatarByUserName(userInfo.userName);
            binding.dialogCallIcon.setImageDrawable(userIcon);
        }

        if (callType == ZegoCallType.Voice) {
            binding.dialogCallType.setText(R.string.zego_voice_call);
        } else {
            binding.dialogCallType.setText(R.string.zego_video_call);
        }
    }

    public ZegoUserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setListener(OnReceiveCallViewClickedListener listener) {
        this.listener = listener;
    }

    public interface OnReceiveCallViewClickedListener {

        void onAcceptAudioClicked();

        void onAcceptVideoClicked();

        void onDeclineClicked();

        void onWindowClicked();
    }
}
