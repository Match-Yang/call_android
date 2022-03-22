package im.zego.callsdk.service;

import android.app.Application;
import android.util.Log;
import com.google.gson.Gson;
import im.zego.callsdk.ZegoZIMManager;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * Class LiveAudioRoom business logic management.
 * <p> Description: This class contains the LiveAudioRoom business logics, manages the service instances of different
 * modules, and also distributing the data delivered by the SDK.
 */
public class ZegoServiceManager {

    private static volatile ZegoServiceManager singleton = null;

    private ZegoServiceManager() {
    }

    /**
     * Get the ZegoRoomManager singleton instance.
     * <p> Description: This method can be used to get the ZegoRoomManager singleton instance.
     * <p> Call this method at: Any time
     *
     * @return
     */
    public static ZegoServiceManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoServiceManager.class) {
                if (singleton == null) {
                    singleton = new ZegoServiceManager();
                }
            }
        }
        return singleton;
    }

    /**
     * The user information management instance, contains the in-room user information management, logged-in user
     * information and other business logics.
     */
    public ZegoUserService userService;
    public ZegoCallService callService;
    public ZegoRoomService roomService;
    public ZegoDeviceService deviceService;
    public Gson mGson = new Gson();
    private static final String TAG = "ZegoService";

    /**
     * Initialize the SDK.
     * <p>Call this method at: Before you log in. We recommend you call this method when the application starts.
     *
     * @param appID       refers to the project ID. To get this, go to ZEGOCLOUD Admin Console:
     *                    https://console.zego.im/dashboard?lang=en
     * @param appSign     refers to the secret key for authentication. To get this, go to ZEGOCLOUD Admin Console:
     *                    https://console.zego.im/dashboard?lang=en
     * @param application th app context
     */
    public void init(long appID, String appSign, Application application) {
        userService = new ZegoUserServiceImpl();
        callService = new ZegoCallServiceImpl();
        roomService = new ZegoRoomServiceImpl();
        deviceService = new ZegoDeviceServiceImpl();

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.COMMUNICATION;
        profile.application = application;
        ZegoExpressEngine.createEngine(profile, new IZegoEventHandler() {
            @Override
            public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
                ZegoStreamQualityLevel downstreamQuality) {
                super.onNetworkQuality(userID, upstreamQuality, downstreamQuality);

            }

            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);

            }


            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);

            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                for (ZegoStream zegoStream : streamList) {
                    Log.d(TAG, "onRoomStreamUpdate: " + zegoStream.streamID + ",updateType:" + updateType);
                }
            }
        });
    }

    /**
     * The method to deinitialize the SDK.
     * <p> Description: This method can be used to deinitialize the SDK and release the resources it occupies.</>
     * <p> Call this method at: When the SDK is no longer be used. We recommend you call this method when the
     * application exits.</>
     */
    public void unInit() {
        ZegoZIMManager.getInstance().destroyZIM();
        ZegoExpressEngine.destroyEngine(null);
    }

    /**
     * Upload local logs to the ZEGOCLOUD server.
     * <p>Description: You can call this method to upload the local logs to the ZEGOCLOUD Server for troubleshooting
     * when exception occurs.</>
     * <p>Call this method at: When exceptions occur </>
     *
     * @param callback refers to the callback that be triggered when the logs are upload successfully or failed to
     *                 upload logs.
     */
    public void uploadLog(final ZegoCallback callback) {
        ZegoZIMManager.getInstance().zim
            .uploadLog(errorInfo -> callback.onResult(errorInfo.code.value()));
        ZegoExpressEngine.getEngine().uploadLog();
    }
}
