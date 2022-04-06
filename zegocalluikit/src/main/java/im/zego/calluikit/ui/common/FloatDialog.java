package im.zego.calluikit.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import im.zego.calluikit.R;

public class FloatDialog extends Dialog {
    public FloatDialog(@NonNull Context context, View view) {
        super(context, R.style.TipsStyle);
        initDialog(view);
    }

    private void initDialog(View view) {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(view);

        view.measure(0, 0);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }
}