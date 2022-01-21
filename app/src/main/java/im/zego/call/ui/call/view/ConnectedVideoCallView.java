package im.zego.call.ui.call.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.databinding.LayoutConnectedVideoCallBinding;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;

public class ConnectedVideoCallView extends ConstraintLayout {

    private LayoutConnectedVideoCallBinding binding;
    private ZegoUserInfo userInfo;
    private ConnectedVideoCallLister listener;
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
                    if (listener != null) {
                        listener.onEndCall(errorCode);
                    }
                });
            }
        });
        ZegoUserInfo localUserInfo = userService.localUserInfo;
        binding.callVideoCamera.setSelected(localUserInfo.camera);
        binding.callVideoCamera.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            userService.cameraOperate(!selected, errorCode -> {

            });
        });
        binding.callVideoMic.setSelected(localUserInfo.mic);
        binding.callVideoMic.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            v.setSelected(!selected);
            userService.micOperate(!selected, errorCode -> {

            });
        });
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
                binding.callVideoViewSmallName.setText("");
                userService.startPlayingUserMedia(userService.localUserInfo.userID, binding.textureViewBig);
                userService.startPlayingUserMedia(userInfo.userID, binding.textureViewSmall);
            } else {
                binding.callVideoViewSmallName.setText(userInfo.userName);
                userService.startPlayingUserMedia(userService.localUserInfo.userID, binding.textureViewSmall);
                userService.startPlayingUserMedia(userInfo.userID, binding.textureViewBig);
            }
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            if (visibility == View.VISIBLE) {
                if (isSelfCenter) {
                    userService.startPlayingUserMedia(userService.localUserInfo.userID, binding.textureViewBig);
                    userService.startPlayingUserMedia(userInfo.userID, binding.textureViewSmall);
                } else {
                    userService.startPlayingUserMedia(userService.localUserInfo.userID, binding.textureViewSmall);
                    userService.startPlayingUserMedia(userInfo.userID, binding.textureViewBig);
                }
            }
        }
    }

    public void setListener(ConnectedVideoCallLister lister) {
        this.listener = lister;
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void onLocalUserChanged(ZegoUserInfo localUserInfo) {
        Log.d("TAG", "onLocalUserChanged() called with: localUserInfo = [" + localUserInfo + "]");
        binding.callVideoMic.setSelected(localUserInfo.mic);
        binding.callVideoCamera.setSelected(localUserInfo.camera);
    }

    public interface ConnectedVideoCallLister {

        void onEndCall(int errorCode);
    }
}
