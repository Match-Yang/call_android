package im.zego.call.ui.user;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import im.zego.call.R;
import im.zego.calluikit.utils.AvatarHelper;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.List;

public class OnlineUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ZegoUserInfo> userInfoList;
    private static final int USER = 0;
    private static final int NONE = 1;

    public OnlineUserAdapter(List<ZegoUserInfo> list) {
        if (list == null) {
            this.userInfoList = new ArrayList<>();
        } else {
            this.userInfoList = list;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_user, parent, false);
        }
        return new ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View itemView = holder.itemView;
        int viewType = getItemViewType(position);
        if (viewType == USER) {
            ZegoUserInfo userInfo = userInfoList.get(position);
            TextView userIDTextView = itemView.findViewById(R.id.item_online_user_id);
            TextView userNameTextView = itemView.findViewById(R.id.item_online_user_name);
            ImageView userIconIv = itemView.findViewById(R.id.item_online_user_icon);
            userIDTextView.setText("ID:" + userInfo.userID);
            userNameTextView.setText(userInfo.userName);
            Drawable userIcon = AvatarHelper.getAvatarByUserName(userInfo.userName);
            userIconIv.setImageDrawable(userIcon);

        }
    }

    @Override
    public int getItemCount() {
        if (userInfoList.size() > 0) {
            return userInfoList.size();
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (userInfoList.size() > 0) {
            return USER;
        } else {
            return NONE;
        }
    }

    public ZegoUserInfo getUserInfo(int index) {
        return userInfoList.get(index);
    }

    public void updateList(List<ZegoUserInfo> list) {
        this.userInfoList.clear();
        this.userInfoList.addAll(list);
        notifyDataSetChanged();
    }
}
