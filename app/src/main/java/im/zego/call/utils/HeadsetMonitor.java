package im.zego.call.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

public class HeadsetMonitor {

    private static final String TAG = "AudioDeviceMonitor";
    private HeadSetListener listener;
    private HeadsetPlugReceiver receiver;

    public HeadsetMonitor() {
        receiver = new HeadsetPlugReceiver();
    }

    public void setListener(HeadSetListener listener) {
        this.listener = listener;
    }

    private void registerHeadsetPlugReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }

    private void unRegisterHeadsetPlugReceiver(Context context) {
        context.unregisterReceiver(receiver);
    }

    public boolean isHeadsetOn(Context context) {
        boolean wiredHeadsetOn = isWiredHeadsetOn(context);
        boolean bluetoothHeadsetOn = isBluetoothHeadsetOn(context);
        Log.d(TAG,  "isHeadsetOn() returned,wiredHeadsetOn:" + wiredHeadsetOn
            + ",bluetoothHeadsetOn:" + bluetoothHeadsetOn);
        return wiredHeadsetOn || bluetoothHeadsetOn;
    }

    private boolean isWiredHeadsetOn(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        return mAudioManager.isWiredHeadsetOn();
    }

    private boolean isBluetoothHeadsetOn(Context context) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            return false;
        }
        int connectionState = defaultAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        return BluetoothProfile.STATE_CONNECTED == connectionState;
    }

    private class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,  "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                int state = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
                if (BluetoothProfile.STATE_CONNECTED == state) {
                    Toast.makeText(context, "bluetooth headset  connected", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onHeadSetConnected(true);
                    }
                } else if (BluetoothProfile.STATE_DISCONNECTED == state) {
                    Toast.makeText(context, "bluetooth headset not connected", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onHeadSetConnected(false);
                    }
                }

            } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        Toast.makeText(context, "wired headset not connected", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onHeadSetConnected(false);
                        }
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        Toast.makeText(context, "wired headset  connected", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onHeadSetConnected(true);
                        }
                    }
                }
            }

        }
    }

    public interface HeadSetListener {

        void onHeadSetConnected(boolean connected);
    }
}
