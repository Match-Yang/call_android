package im.zego.call.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.ui.call.CallActivity;
import im.zego.call.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMErrorCode;

public class ReceiveCallDialog extends Dialog {

    private ZegoUserInfo userInfo;
    private ZegoCallType callType;

    public ReceiveCallDialog(@NonNull Context context, ZegoUserInfo userInfo, ZegoCallType callType) {
        super(context, R.style.TipsStyle);
        this.userInfo = userInfo;
        this.callType = callType;
        initDialog(context);
    }

    private void initDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_call, null, false);
        View acceptVoice = view.findViewById(R.id.dialog_call_accept_voice);
        View acceptVideo = view.findViewById(R.id.dialog_call_accept_video);
        if (callType == ZegoCallType.Audio) {
            acceptVoice.setVisibility(View.VISIBLE);
            acceptVideo.setVisibility(View.GONE);
        } else {
            acceptVoice.setVisibility(View.GONE);
            acceptVoice.setVisibility(View.VISIBLE);
        }
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setContentView(view);

        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);

        TextView nameTv = view.findViewById(R.id.dialog_call_name);
        nameTv.setText(userInfo.userName);
        ImageView iconIv = view.findViewById(R.id.dialog_call_icon);
        Drawable userIcon = AvatarHelper.getAvatarByUserName(userInfo.userName);
        iconIv.setImageDrawable(userIcon);
        TextView callTypeTv = view.findViewById(R.id.dialog_call_type);
        if (callType == ZegoCallType.Audio) {
            callTypeTv.setText(R.string.zego_voice_call);
        } else {
            callTypeTv.setText(R.string.zego_video_call);
        }

        acceptVoice.setOnClickListener(v -> {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            String token = AuthInfoManager.getInstance().generateJoinRoomToken(userService.localUserInfo.userID);
            userService.responseCall(ZegoResponseType.Accept, userInfo.userID, token, errorCode -> {
                if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                    CallActivity.startCallActivity(CallActivity.TYPE_CONNECTED_VOICE, userInfo);
                } else {
                    ToastUtils.showShort("responseCall " + errorCode);
                }
                dismiss();
            });
        });
        acceptVideo.setOnClickListener(v -> {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            String token = AuthInfoManager.getInstance().generateJoinRoomToken(userService.localUserInfo.userID);
            userService.responseCall(ZegoResponseType.Accept, userInfo.userID, token, errorCode -> {
                if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                    CallActivity.startCallActivity(CallActivity.TYPE_CONNECTED_VIDEO, userInfo);
                } else {
                    ToastUtils.showShort("responseCall " + errorCode);
                }
                dismiss();
            });
        });
        view.findViewById(R.id.dialog_call_decline).setOnClickListener(v -> {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            userService.responseCall(ZegoResponseType.Decline, userInfo.userID, null, errorCode -> {
                dismiss();
            });
        });
    }
}
