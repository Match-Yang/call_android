package im.zego.calluikit.ui.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;

import java.util.Objects;

import im.zego.calluikit.R;
import im.zego.calluikit.ui.dialog.base.BaseDialog;


public class CommonStringArrayDialog extends BaseDialog {

    private AppCompatImageView ivBack;
    private TextView tvTitle;
    private RecyclerView recyclerView;

    private String title;
    private String checkedString;
    private String[] stringArray;

    private IStringArrayListener listener;

    public CommonStringArrayDialog(Context context, String title, String checkedString, String[] stringArray, IStringArrayListener listener) {
        super(context);
        this.title = title;
        this.checkedString = checkedString;
        this.stringArray = stringArray;
        this.listener = listener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_common_string_array;
    }

    @Override
    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected void initView() {
        super.initView();
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        recyclerView = findViewById(R.id.recycler_view);
    }

    @Override
    protected void initData() {
        super.initData();
        tvTitle.setText(title);
        StringAdapter stringAdapter = new StringAdapter();
        recyclerView.setAdapter(stringAdapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        ivBack.setOnClickListener(v -> dismiss());
    }

    public interface IStringArrayListener {
        void onItemSelected(String checkedString);
    }

    class StringAdapter extends RecyclerView.Adapter<StringViewHolder> {

        @NonNull
        @Override
        public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string_array, parent, false);
            return new StringViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StringViewHolder holder, int position) {
            String title = stringArray[position];
            if (Objects.equals(title, checkedString)) {
                holder.arrayIvCheck.setVisibility(View.VISIBLE);
                holder.arrayTvTitle.setTextColor(ColorUtils.getColor(R.color.item_string_selected_color));
            } else {
                holder.arrayIvCheck.setVisibility(View.GONE);
                holder.arrayTvTitle.setTextColor(ColorUtils.getColor(R.color.item_string_default_color));
            }
            holder.arrayTvTitle.setText(title);
            holder.itemView.setOnClickListener(v -> {
                checkedString = title;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onItemSelected(title);
                }
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return stringArray.length;
        }
    }

    static class StringViewHolder extends RecyclerView.ViewHolder {
        private TextView arrayTvTitle;
        private AppCompatImageView arrayIvCheck;

        public StringViewHolder(@NonNull View itemView) {
            super(itemView);
            arrayTvTitle = itemView.findViewById(R.id.array_tv_title);
            arrayIvCheck = itemView.findViewById(R.id.array_iv_check);
        }
    }
}
