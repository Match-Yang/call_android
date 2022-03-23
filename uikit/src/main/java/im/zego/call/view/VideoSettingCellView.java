package im.zego.call.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import im.zego.call.R;
import im.zego.call.databinding.LayoutVideoSettingCellViewBinding;


public class VideoSettingCellView extends ConstraintLayout {
    private static final int CELL_VIEW_TYPE_TEXT_WITH_ARROW = 1;
    private static final int CELL_VIEW_TYPE_TEXT_WITH_SWITCH = 2;

    private final LayoutVideoSettingCellViewBinding binding;

    private int cellViewType;
    private String title;
    private String content;
    private boolean switchChecked;

    private ISettingCellViewListener listener;

    public VideoSettingCellView(@NonNull Context context) {
        this(context, null);
    }

    public VideoSettingCellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSettingCellView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = LayoutVideoSettingCellViewBinding.inflate(LayoutInflater.from(context), this, true);
        init(context, attrs);
        updateUI();
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SettingCellView, 0, 0);
        try {
            cellViewType = typedArray.getInt(R.styleable.SettingCellView_cell_view_type, CELL_VIEW_TYPE_TEXT_WITH_ARROW);
            title = typedArray.getString(R.styleable.SettingCellView_cell_view_title);
            content = typedArray.getString(R.styleable.SettingCellView_cell_view_content);
            switchChecked = typedArray.getBoolean(R.styleable.SettingCellView_cell_view_switch_checked, false);
        } finally {
            typedArray.recycle();
        }
    }

    private void updateUI() {
        if (cellViewType == CELL_VIEW_TYPE_TEXT_WITH_ARROW) {
            binding.cellIvArrow.setVisibility(VISIBLE);
            binding.cellTvContent.setVisibility(VISIBLE);
            binding.cellSwitch.setVisibility(GONE);
            binding.cellTvContent.setText(content);
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(switchChecked);
                }
            });
        } else {
            binding.cellIvArrow.setVisibility(GONE);
            binding.cellTvContent.setVisibility(GONE);
            binding.cellSwitch.setVisibility(VISIBLE);
            binding.cellSwitch.setChecked(switchChecked);
            binding.cellSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                switchChecked = isChecked;
                if (listener != null) {
                    listener.onClick(switchChecked);
                }
            });
        }

        binding.cellTvTitle.setText(title);
    }

    @Override
    public void setEnabled(boolean enabled) {
        binding.cellTvTitle.setAlpha(enabled ? 1F : 0.5F);
        if (cellViewType == CELL_VIEW_TYPE_TEXT_WITH_SWITCH) {
            if (!enabled) {
                binding.cellSwitch.setChecked(false);
            }
            binding.cellSwitch.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public void setContent(String content) {
        this.content = content;
        updateUI();
    }

    public void setChecked(boolean checked) {
        binding.cellSwitch.setChecked(checked);
    }

    public void setListener(ISettingCellViewListener listener) {
        this.listener = listener;
    }

    public interface ISettingCellViewListener {
        void onClick(boolean isChecked);
    }
}
