package im.zego.call.ui.call;


import android.app.Activity;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import com.blankj.utilcode.util.ActivityUtils;
import im.zego.callsdk.model.ZegoUserInfo;
import java.util.ArrayList;
import java.util.List;

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

    public boolean needNotification() {
        return callState == TYPE_INCOMING_CALLING_VOICE ||
            callState == TYPE_INCOMING_CALLING_VIDEO ||
            callState == TYPE_CONNECTED_VOICE ||
            callState == TYPE_CONNECTED_VIDEO ||
            callState == TYPE_OUTGOING_CALLING_VOICE ||
            callState == TYPE_OUTGOING_CALLING_VIDEO;
    }

    public void setCallState(ZegoUserInfo userInfo, int callState) {
        int beforeState = this.callState;
        if (userInfo != null) {
            this.userInfo = userInfo;
        }
        this.callState = callState;
        if (beforeState != callState && listeners.size() > 0) {
            for (CallStateChangedListener listener : listeners) {
                listener.onCallStateChanged(beforeState, callState);
            }
        }
        if (callState == TYPE_INCOMING_CALLING_VIDEO || callState == TYPE_INCOMING_CALLING_VOICE) {
            playRingTone();
        }else {
            stopRingTone();
        }
    }

    private MediaPlayer mediaPlayer;

    private void playRingTone() {
        Activity topActivity = ActivityUtils.getTopActivity();
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(topActivity, RingtoneManager.TYPE_RINGTONE);
        mediaPlayer = MediaPlayer.create(topActivity, ringtoneUri);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void stopRingTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public ZegoUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(ZegoUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void addListener(CallStateChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CallStateChangedListener listener) {
        listeners.remove(listener);
    }

    public interface CallStateChangedListener {

        void onCallStateChanged(int before, int after);
    }
}