package im.zego.call.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.ResourceUtils;

public final class AvatarHelper {

    private static final int MAX_INDEX = 6;
    private static final String TAG = "AvatarHelper";

    public static Drawable getAvatarByUserName(String userName) {
        int resourceID = getResourceID(userName, false);
        Drawable drawable = ResourceUtils.getDrawable(resourceID);
        return drawable;
    }

    public static Drawable getFullAvatarByUserName(String userName) {
        int resourceID = getResourceID(userName, true);
        Drawable drawable = ResourceUtils.getDrawable(resourceID);
        return drawable;
    }

    public static int getResourceID(String userName, boolean full) {
        String md5String = EncryptUtils.encryptMD5ToString(userName).toLowerCase();
        int index = Math.abs(userName.charAt(0) % MAX_INDEX) + 1;
        String name;
        if (full) {
            name = "user_icon_" + (index) + "_big";
        } else {
            name = "user_icon_" + (index);
        }
        Log.d(TAG, "getResourceIndex() called with: userName = [" + userName + "], full = [" + full + "],md5String:"
            + md5String + ",index:" + index + ",name: " + name);
        return ResourceUtils.getDrawableIdByName(name);

    }

    public static int getBlurResourceID(String userName) {
        String md5String = EncryptUtils.encryptMD5ToString(userName).toLowerCase();
        int index = Math.abs(userName.charAt(0) % MAX_INDEX) + 1;
        String name = "user_icon_" + (index) + "_blur";
        Log.d(TAG, "getResourceIndex() called with: userName = [" + userName + "],md5String:"
            + md5String + ",index:" + index + ",name: " + name);
        return ResourceUtils.getDrawableIdByName(name);

    }
}