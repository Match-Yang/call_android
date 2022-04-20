package im.zego.calluikit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.callsdk.utils.CallUtils;
import im.zego.calluikit.R;
import im.zego.calluikit.ui.call.CallStateManager;

/**
 * foreground service, only used to keep process foreground.
 */
public class ForegroundService extends Service {

    private String CHANNEL_ID = "channel 2";
    private String CHANNEL_NAME = "channel2 name";
    private String CHANNEL_DESC = "channel2 desc";

    @Override
    public void onCreate() {
        super.onCreate();
        CallUtils.d("onCreate() called");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent appIntent = new Intent();
        try {
            appIntent = new Intent(this, Class.forName(ActivityUtils.getLauncherActivity()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingIntent = PendingIntent.getActivity(this, 0, appIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true);

        startForeground(65536, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
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
            channel.setSound(null, null);
            channel.enableVibration(false);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}