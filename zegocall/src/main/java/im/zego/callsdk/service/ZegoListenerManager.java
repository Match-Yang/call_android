package im.zego.callsdk.service;

import android.util.Log;
import im.zego.callsdk.callback.ZegoNotifyListener;
import im.zego.callsdk.listener.ZegoListener;
import im.zego.callsdk.listener.ZegoListenerUpdater;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoListenerManager implements ZegoListener, ZegoListenerUpdater {

    private Map<String, List<ZegoNotifyListener>> listenerMap = new HashMap<>();
    private static final String TAG = "ListenerManager";

    private static volatile ZegoListenerManager singleton = null;

    private ZegoListenerManager() {
    }

    public static ZegoListenerManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoListenerManager.class) {
                if (singleton == null) {
                    singleton = new ZegoListenerManager();
                }
            }
        }
        return singleton;
    }

    public static final String RECEIVE_CALL = "/call/notify_call_invited";
    public static final String CANCEL_CALL = "/call/notify_call_canceled";
    public static final String ACCEPT_CALL = "/call/notify_call_accept";
    public static final String DECLINE_CALL = "/call/notify_call_decline";
    public static final String END_CALL = "/call/notify_call_end";
    public static final String TIMEOUT_CALL = "/call/notify_timeout";

    @Override
    public void receiveUpdate(String path, Object parameter) {
        Log.d(TAG, "receiveUpdate() called with: path = [" + path + "], parameter = [" + parameter + "]");
        List<ZegoNotifyListener> ListenerList = listenerMap.get(path);
        if (ListenerList != null) {
            for (ZegoNotifyListener listener : ListenerList) {
                listener.onNotifyInvoked(parameter);
            }
        }
    }

    @Override
    public void addListener(String path, ZegoNotifyListener listener) {
        List<ZegoNotifyListener> listenerList = listenerMap.get(path);
        if (listenerList != null) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        } else {
            listenerList = new ArrayList<>();
            listenerList.add(listener);
            listenerMap.put(path, listenerList);
        }
    }

    @Override
    public void removeListener(String path, ZegoNotifyListener listener) {
        List<ZegoNotifyListener> listenerList = listenerMap.get(path);
        if (listenerList != null) {
            listenerList.remove(listenerList);
        }
    }
}
