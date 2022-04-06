package im.zego.calluikit.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import im.zego.calluikit.databinding.LayoutDialogLoadingBinding;

public class LoadingDialog extends Dialog {

    private LayoutDialogLoadingBinding binding;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        initView();
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initView();
    }

    private void initView() {
        binding = LayoutDialogLoadingBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable());
        window.setDimAmount(0f);
    }

    public void setProgressVisibility(boolean visible) {
        binding.progress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setLoadingText(String text) {
        binding.content.setText(text);
    }
}
