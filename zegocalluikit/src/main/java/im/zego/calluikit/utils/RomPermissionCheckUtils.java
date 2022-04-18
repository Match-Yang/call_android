package im.zego.calluikit.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import im.zego.callsdk.utils.CallUtils;
import java.lang.reflect.Method;
import java.util.Objects;

public class RomPermissionCheckUtils {

    public static boolean checkBackgroundStartPermission(Context context) {
        CallUtils.d("checkBackgroundStartPermission() called: " + Build.BRAND);
        if (isXiaomi()) {
            boolean checkXiaomi = checkXiaomi(context);
            return checkXiaomi;
        } else if (isVivo()) {
            return true;
        } else if (Objects.equals(Build.BRAND, "oppo")) {
            return true;
        } else {
            return true;
        }
    }

    private static boolean isVivo() {
        return Objects.equals(Build.BRAND, "vivo");
    }

    private static boolean isXiaomi() {
        return Objects.equals(Build.BRAND, "Xiaomi");
    }

    public static void jumpToPermissionsActivity(Context context) {
        if (isXiaomi()) {
            try {
                // MIUI 8
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivitycom.miui.permcenter.permissions.PermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(localIntent);
            } catch (Exception e) {
                try {
                    // MIUI 5/6/7
                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    localIntent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    localIntent.putExtra("extra_pkgname", context.getPackageName());
                    context.startActivity(localIntent);
                } catch (Exception e1) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            }
        } else if (isVivo()) {
        } else if (Objects.equals(Build.BRAND, "oppo")) {
        } else {
        }

    }

    private static boolean checkXiaomi(Context context) {
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10021;
            Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
            Integer result = (Integer) method.invoke(ops, op, android.os.Process.myUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            CallUtils.d("checkXiaomi, not support");
        }
        return false;
    }
}
