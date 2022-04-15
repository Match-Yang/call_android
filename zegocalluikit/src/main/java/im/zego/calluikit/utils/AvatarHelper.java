package im.zego.calluikit.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.blankj.utilcode.util.ResourceUtils;
import im.zego.callsdk.utils.CallUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        int index = getIndex(userName);
        String name;
        if (full) {
            name = "user_icon_" + (index % MAX_INDEX + 1) + "_big";
        } else {
            name = "user_icon_" + (index % MAX_INDEX + 1);
        }
        CallUtils.d(
            "getResourceIndex() called with: userName = [" + userName + "], full = [" + full + "],:" + ",index:" + index
                + ",name: " + name);
        return ResourceUtils.getDrawableIdByName(name);

    }

    public static int getBlurResourceID(String userName) {
        int index = getIndex(userName);
        String name = "user_icon_" + (index % MAX_INDEX + 1) + "_blur";
        CallUtils.d( "getResourceIndex() called with: userName = [" + userName + "],index:" + index + ",name: " + name);
        return ResourceUtils.getDrawableIdByName(name);

    }

    public static int getIndex(String userName) {
        byte[] value;
        try {
            value = md5(userName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }

        if (value.length > 0) {
            String hex = bytesToHex(value);
            int value0 = value[0] & 0xff;
            int index = Math.abs(value0 % MAX_INDEX);
            return index;
        } else {
            return 0;
        }
    }

    private static byte[] md5(String input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(input.getBytes());
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}