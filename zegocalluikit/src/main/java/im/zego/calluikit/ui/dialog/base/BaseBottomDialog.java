package im.zego.calluikit.ui.dialog.base;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import im.zego.calluikit.R;


public abstract class BaseBottomDialog extends BottomSheetDialog {
    public BaseBottomDialog(@NonNull Context context) {
        super(context, R.style.BottomSheetDialog);
    }

    public BaseBottomDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData();
        initListener();

        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setCanceledOnTouchOutside(canceledOnTouchOutside());
        setCancelable(cancelable());
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