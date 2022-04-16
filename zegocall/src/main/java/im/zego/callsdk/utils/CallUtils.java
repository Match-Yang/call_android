package im.zego.callsdk.utils;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import im.zego.callsdk.BuildConfig;
import im.zego.zegoexpress.ZegoExpressErrorCode;
import java.lang.reflect.Field;

public class CallUtils {

    private static final String TAG = "CallLog";

    private static final Field[] expressErrorFields;
    private static Application application;

    static {
        expressErrorFields = ZegoExpressErrorCode.class.getDeclaredFields();
    }

    public static void e(String string) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, string);
        }
    }

    public static void d(String string) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, string);
        }
    }

    public static void w(String string) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, string);
        }
    }

    public static String printError(int errorCode) {
        String result = "";
        if (BuildConfig.DEBUG) {
            try {
                for (int i = 0; i < expressErrorFields.length; i++) {
                    Field field = expressErrorFields[i];
                    int value = Integer.parseInt(field.get(ZegoExpressErrorCode.class).toString());
                    String name = field.getName();
                    if (errorCode == value) {
                        result = name;
                        String msg = "\nname = " + name + ",errorCode = " + value + ",\n "
                            + "======= \n"
                            + "You can view the exact cause of the error through the link below\n"
                            + " https://docs.zegocloud.com/article/5548?w=" + errorCode
                            + "\n======= ";
                        Log.e(TAG, msg);
                        if (application != null) {
                            Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void init(Application application) {
        CallUtils.application = application;
    }
}
