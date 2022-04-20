package im.zego.calluikit.ui.call;


import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.blankj.utilcode.util.ActivityUtils;

import im.zego.callsdk.utils.CallUtils;
import java.util.ArrayList;
import java.util.List;

import im.zego.callsdk.model.ZegoUserInfo;
import java.util.Objects;

public class CallStateManager {

    public static final int TYPE_NO_CALL = 0;

    public static final int TYPE_INCOMING_CALLING_VOICE = 1;
    public static final int TYPE_INCOMING_CALLING_VIDEO = 2;
    public static final int TYPE_CONNECTED_VOICE = 3;
    public static final int TYPE_CONNECTED_VIDEO = 4;
    public static final int TYPE_OUTGOING_CALLING_VOICE = 5;
    public static final int TYPE_OUTGOING_CALLING_VIDEO = 6;

    public static final int TYPE_CALL_CANCELED = 7;
    public static final int TYPE_CALL_COMPLETED = 8;
    public static final int TYPE_CALL_MISSED = 9;
    public static final int TYPE_CALL_DECLINE = 10;
    public static final int TYPE_CALL_BUSY = 11;
    private Vibrator vibrator;

    private CallStateManager() {
    }

    private static final class Holder {

        private static final CallStateManager INSTANCE = new CallStateManager();
    }

    public static CallStateManager getInstance() {
        return Holder.INSTANCE;
    }

    private int callState = 0;
    private ZegoUserInfo userInfo;
    private List<CallStateChangedListener> listeners = new ArrayList<>();

    public int getCallState() {
        return callState;
    }

    public boolean isInACallStream() {
        return isIncoming() || isConnected() || isOutgoing();
    }

    public boolean isOutgoing() {
        return callState == TYPE_OUTGOING_CALLING_VOICE ||
            callState == TYPE_OUTGOING_CALLING_VIDEO;
    }

    public boolean isConnected() {
        return callState == TYPE_CONNECTED_VIDEO ||
            callState == TYPE_CONNECTED_VOICE;
    }

    public boolean isIncoming() {
        return callState == TYPE_INCOMING_CALLING_VIDEO ||
            callState == TYPE_INCOMING_CALLING_VOICE;
    }

    public void setCallState(ZegoUserInfo userInfo, int callState) {
        int beforeState = this.callState;
        CallUtils.d(
            "onCallStateChanged() called with:" + userInfo + "， before = [" + beforeState
                + "], after = [" + callState + "]");
        if (!Objects.equals(this.userInfo, userInfo)) {
            this.userInfo = userInfo;
        }
        this.callState = callState;
        if (callState == TYPE_INCOMING_CALLING_VIDEO || callState == TYPE_INCOMING_CALLING_VOICE) {
            playRingTone();
        } else {
            stopRingTone();
        }
        if (listeners.size() > 0) {
            if (beforeState != callState) {
                for (CallStateChangedListener listener : listeners) {
                    listener.onCallStateChanged(userInfo, beforeState, callState);
                }
            }
        }
    }

    private MediaPlayer mediaPlayer;

    private void playRingTone() {
        Activity topActivity = ActivityUtils.getTopActivity();
        AudioManager audioManager = (AudioManager) topActivity.getSystemService(Context.AUDIO_SERVICE);
        if (vibrator == null) {
            vibrator = (Vibrator) topActivity.getSystemService(Service.VIBRATOR_SERVICE);
        }

        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(topActivity, RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri != null && mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(topActivity, ringtoneUri);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                vibrateDevice();
            }
        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            vibrateDevice();
        } else {

        }
    }

    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
            vibrator.vibrate(new long[]{600, 600, 600, 600}, 0);
        }
    }

    public void stopRingTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    public ZegoUserInfo getUserInfo() {
        return userInfo;
    }

    public void addListener(CallStateChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CallStateChangedListener listener) {
        listeners.remove(listener);
    }

    public interface CallStateChangedListener {

        void onCallStateChanged(ZegoUserInfo userInfo, int before, int after);
    }
}