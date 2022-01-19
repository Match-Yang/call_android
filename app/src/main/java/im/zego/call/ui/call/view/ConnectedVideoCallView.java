package im.zego.call.ui.call.view;

import android.content.Context;
import android.util.AttributeSet;
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

        binding.callVideoHangUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                userService.endCall(errorCode -> {
                    if (listener != null) {
                        listener.onEndCall(errorCode);
                    }
                });
            }
        });
    }

    public void setListener(ConnectedVideoCallLister lister) {
        this.listener = lister;
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public interface ConnectedVideoCallLister {

        void onEndCall(int errorCode);
    }
}
