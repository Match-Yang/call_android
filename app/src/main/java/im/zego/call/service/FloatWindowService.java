package im.zego.call.service;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.call.R;
import im.zego.call.ui.call.CallStateManager;
import im.zego.call.ui.login.LoginActivity;

/**
 * foreground service used to keep process foreground.
 */
public class FloatWindowService extends Service {

    public static boolean isStarted = false;
    private static final String TAG = "FloatWindowService";
    private int notificationId = 888;
    private String CHANNEL_ID = "channel 2";
    private String CHANNEL_NAME = "channel2 name";
    private String CHANNEL_DESC = "channel2 desc";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        isStarted = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Activity topActivity = ActivityUtils.getTopActivity();
        Intent appIntent = new Intent(topActivity, LoginActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(topActivity, 0, appIntent, 0);

        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_dialog_voice_accept)
            .setContentTitle(context.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true);

        startForeground(notificationId, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
        Log.d(TAG, "onDestroy() called");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            Activity topActivity = ActivityUtils.getTopActivity();
            NotificationManager notificationManager = topActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}