package im.zego.callsdk;

import android.app.Application;
import im.zego.zim.ZIM;

/**
 * This class contains and manages the ZIM SDK instance objects, so that ZIM related methods can be called more
 * effectively according to different modules.
 */
public class ZegoZIMManager {

    private static volatile ZegoZIMManager singleton = null;

    private ZegoZIMManager() {
    }

    /**
     * Get the ZegoZIMManager singleton instance.This method can be used to get the ZegoZIMManager singleton
     * instance.<br> Call this method at: When you need to use the ZegoZIMManager singleton instance.
     *
     * @return singleton instance
     */
    public static ZegoZIMManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoZIMManager.class) {
                if (singleton == null) {
                    singleton = new ZegoZIMManager();
                }
            }
        }
        return singleton;
    }

    public ZIM zim;

    /**
     * Create the ZIM SDK instance.<p>Description: You need to call this method to initialize the ZIM SDK first before
     * you log in, create a room, join a room, send messages and other operations with ZIM SDK. This method need to be
     * used in conjunction with the [destroyZIM] method, which is to make sure that the current process is running only
     * one ZIM SDK instance.
     * <p>Call this method at: Before you calling the ZIM SDK methods. We recommend you call this method when the
     * application starts.
     *
     * @param appID       appID refers to the ID of your project. To get this, go to ZEGOCLOUD Admin Console:
     *                    https://console.zego.im/dashboard?lang=en
     * @param application app application context
     */
    public void createZIM(long appID, Application application) {
        zim = ZIM.create(appID, application);
    }

    /**
     * Destroy the ZIM SDK instance
     * <p> Description: This method can be used to destroy the ZIM SDK instance and release the resources it occupies.
     * <p> Call this method at: When the ZIM SDK is no longer be used. We recommend you call this method when the
     * application exits.
     */
    public void destroyZIM() {
        if (zim != null) {
            zim.destroy();
        }
    }
}
