package im.zego.call.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.blankj.utilcode.util.ResourceUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AvatarHelper {

    private static final int MAX_INDEX = 6;

    public static Drawable getAvatarByUserName(String userName) {
        return ResourceUtils.getDrawable(getAvatarIdByUserName(userName, false));
    }

    public static Drawable getFullAvatarByUserName(String userName) {
        return ResourceUtils.getDrawable(getAvatarIdByUserName(userName, true));
    }

    private static int getAvatarIdByUserName(String userName, boolean full) {
        int index = getIndex(userName);
        int userAvatarId = getUserAvatarResourceId(index, full);
        return userAvatarId;
    }

    private static int getUserAvatarResourceId(int position, boolean full) {
        String name;
        if (full) {
            name = "user_icon_" + (position % MAX_INDEX + 1) + "_big";
        } else {
            name = "user_icon_" + (position % MAX_INDEX + 1);
        }
        Log.d("getUserAvatarId", "getUserAvatarResourceId() returned: " + name);
        return ResourceUtils.getDrawableIdByName(name);
    }

    private static int getIndex(String userName) {
        byte[] value;
        try {
            value = md5(userName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }

        if (value.length > 0) {
            return Math.abs(value[0] % MAX_INDEX);
        } else {
            return 0;
        }
    }

    private static byte[] md5(String input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(input.getBytes());
    }
}