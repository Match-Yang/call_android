package im.zego.calluikit.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.Utils;
import im.zego.calluikit.R;

public class TipsDialog extends Dialog {

    private TextView textView;

    public TipsDialog(@NonNull Context context) {
        this(context, 0);
        initDialog(context);
    }

    public TipsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId == 0 ? R.style.TipsStyle : themeResId);
        initDialog(context);
    }

    private void initDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_tips_view, null);
        textView = view.findViewById(R.id.tv_toast_message);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(view);

        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        window.setAttributes(layoutParams);
        window.setGravity(Gravity.TOP);
    }

    public void showColorTips(TipsMessageType type, String text) {
        textView.setText(text);
        if (type == TipsMessageType.NORMAL) {
            textView.setBackgroundColor(Utils.getApp().getResources().getColor(R.color.light_green));
        } else {
            textView.setBackgroundColor(Utils.getApp().getResources().getColor(R.color.light_red));
        }
    }

    public enum TipsMessageType {
        NORMAL, WARN
    }
}
