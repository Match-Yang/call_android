package im.zego.callsdk.service;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.listener.ZegoListener;
import im.zego.callsdk.listener.ZegoListenerUpdater;
import java.util.Map;

public class ZegoListenerManager implements ZegoListener, ZegoListenerUpdater {

    private Map<String, ZegoRequestCallback> listenerMap;
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

    @Override
    public void registerListener(Object listener, String path, ZegoRequestCallback callback) {

    }

    @Override
    public void removeListener(Object listener, String path) {

    }

    @Override
    public void receiveUpdate(String path, Object parameter) {

    }
}
