package im.zego.calluikit.ui.dialog.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import im.zego.calluikit.R;


public abstract class BaseDialog extends Dialog {
    public BaseDialog(@NonNull Context context) {
        this(context, 0);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId == 0 ? R.style.BaseDialog : themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData();
        initListener();

        Window window = getWindow();
        window.setLayout(getLayoutWidth(), getLayoutHeight());
        window.setGravity(getGravity());

        setCanceledOnTouchOutside(canceledOnTouchOutside());
        setCancelable(cancelable());
    }

    protected int getGravity() {
        return Gravity.CENTER;
    }

    protected int getLayoutWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    protected int getLayoutHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    protected boolean canceledOnTouchOutside() {
        return true;
    }

    protected boolean cancelable() {
        return true;
    }

    protected abstract int getLayoutId();

    protected void initView() {
    }

    protected void updateView() {

    }

    protected void initData() {
    }

    protected void initListener() {
    }
}
