package im.zego.call.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import im.zego.call.databinding.LayoutDialogLoadingBinding;

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
    }
}
