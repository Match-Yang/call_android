package im.zego.call.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.blankj.utilcode.util.ResourceUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AvatarHelper {

    private static final int MAX_INDEX = 6;

    public static Drawable getAvatarByUserName(String userName) {
        return ResourceUtils.getDrawable(getAvatarIdByUserName(userName));
    }

    private static int getAvatarIdByUserName(String userName) {
        int index = getIndex(userName);
        Log.d("getIndex", "getAvatarIdByUserName() called with: index = [" + index + "]");
        return getUserAvatarId(index);
    }

    private static int getUserAvatarId(int position) {
        return ResourceUtils.getDrawableIdByName("user_icon_" + (position % MAX_INDEX + 1));
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