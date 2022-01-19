package im.zego.call.ui.call.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.utils.AvatarHelper;
import im.zego.call.databinding.LayoutOutgoingCallBinding;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMErrorCode;

public class OutgoingCallView extends ConstraintLayout {

    private LayoutOutgoingCallBinding binding;
    private ZegoUserInfo userInfo;
    private OutgoingCallLister lister;

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
        binding.callingHangUp.setOnClickListener(v -> {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            userService.cancelCallToUser(userInfo.userID, errorCode -> {
                if (lister != null) {
                    lister.onCancelCall(errorCode);
                }
            });
        });
    }

    public void setLister(OutgoingCallLister lister) {
        this.lister = lister;
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
        String userName = userInfo.userName;
        binding.callUserName.setText(userName);
        Drawable drawable = AvatarHelper.getAvatarByUserName(userName);
        binding.callUserIcon.setImageDrawable(drawable);
    }

    public interface OutgoingCallLister {

        void onCancelCall(int errorCode);
    }
}
