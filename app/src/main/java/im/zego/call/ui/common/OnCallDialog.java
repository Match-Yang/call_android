package im.zego.call.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import im.zego.call.R;
import im.zego.callsdk.model.ZegoCallType;

public class OnCallDialog extends Dialog {

    public OnCallDialog(@NonNull Context context, ZegoCallType type) {
        this(context, 0, type);
        initDialog(context, type);
    }

    public OnCallDialog(@NonNull Context context, int themeResId, ZegoCallType type) {
        super(context, themeResId == 0 ? R.style.TipsStyle : themeResId);
        initDialog(context, type);
    }

    private void initDialog(Context context, ZegoCallType type) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_call, null, false);
        if (type == ZegoCallType.Audio) {
            view.findViewById(R.id.dialog_call_accept_voice).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialog_call_accept_video).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.dialog_call_accept_voice).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_call_accept_voice).setVisibility(View.VISIBLE);
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(view);

        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);
    }
}
