package im.zego.call.ui.call.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import im.zego.call.databinding.LayoutOutgoingCallBinding;

public class OutgingCallView extends ConstraintLayout {

    private LayoutOutgoingCallBinding binding;

    public OutgingCallView(@NonNull Context context) {
        super(context);
        initView();
    }

    public OutgingCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OutgingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public OutgingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LayoutOutgoingCallBinding.inflate(LayoutInflater.from(getContext()),this);
    }
}
