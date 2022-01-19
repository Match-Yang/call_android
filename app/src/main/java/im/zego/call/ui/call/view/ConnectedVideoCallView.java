package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.databinding.LayoutConnectedVideoCallBinding;
import im.zego.call.databinding.LayoutConnectedVoiceCallBinding;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;

public class ConnectedVideoCallView extends ConstraintLayout {

    private LayoutConnectedVideoCallBinding binding;
    private ZegoUserInfo userInfo;

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
        binding = LayoutConnectedVideoCallBinding.inflate(LayoutInflater.from(getContext()),this);
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
