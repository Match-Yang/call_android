package im.zego.call.token;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPStaticUtils;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import im.zego.callsdk.callback.ZegoTokenCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CallUtils;
import im.zego.calluikit.ZegoCallManager;
import im.zego.calluikit.constant.Constants;
import im.zego.zegoexpress.ZegoExpressErrorCode;

public class ZegoTokenManager {
    private static volatile ZegoTokenManager singleton = null;

    private ZegoTokenManager() {
        initTokenFromDisk();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (needUpdateToken()) {
                    forceUpdateToken();
                }
            }
        };
        Timer timer = new Timer();
        // need check more frequently
        timer.schedule(task, 0, 10_000);
    }

    public static ZegoTokenManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoTokenManager.class) {
                if (singleton == null) {
                    singleton = new ZegoTokenManager();
                }
            }
        }
        return singleton;
    }

    private static final long EFFECTIVE_TIME = 24 * 3600L;

    private String token;
    private long expiryTime;
    private String userID;

    public void getToken(@NonNull String userID, @NonNull ZegoTokenCallback callback) {
        this.getToken(userID, false, callback);
    }

    public void getToken(@NonNull String userID, boolean isForceUpdate, @NonNull ZegoTokenCallback callback) {
        if (!TextUtils.isEmpty(this.userID) &&
                Objects.equals(this.userID, userID) &&
                !TextUtils.isEmpty(this.token) &&
                this.expiryTime > System.currentTimeMillis() &&
                !isForceUpdate) {
            callback.onTokenCallback(ZegoExpressErrorCode.CommonSuccess, this.token);
            return;
        }

        this.getTokenFromServer(userID, EFFECTIVE_TIME, (errorCode, token) -> {
            if (errorCode == ZegoExpressErrorCode.CommonSuccess) {
                this.token = token;
                this.userID = userID;
                this.expiryTime = System.currentTimeMillis() / 1000L + EFFECTIVE_TIME;
                saveToken(this.token, this.expiryTime);
                callback.onTokenCallback(errorCode, token);
            } else {
                callback.onTokenCallback(errorCode, null);
            }
        });
    }

    public void reset() {
        saveToken(null, 0);
    }

    private void getTokenFromServer(@NonNull String userID, long effectiveTime, @NonNull ZegoTokenCallback callback) {
        ZegoCallManager.getInstance().getToken(userID, effectiveTime, (errorCode, obj) -> {
            CallUtils.d("getToken onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
            callback.onTokenCallback(errorCode, (String) obj);
        });
    }

    private void saveToken(String token, long effectiveTimeInSeconds) {
        CallUtils.d("saveToken() called with: token = [" + token + "], effectiveTimeInSeconds = [" + effectiveTimeInSeconds + "]");
        if (token == null || effectiveTimeInSeconds == 0) {
            SPStaticUtils.remove(Constants.ZEGO_TOKEN_KEY);
            SPStaticUtils.remove(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY);
        } else {
            long expiryTime = System.currentTimeMillis() / 1000L + effectiveTimeInSeconds;
            SPStaticUtils.put(Constants.ZEGO_TOKEN_KEY, token);
            SPStaticUtils.put(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY, expiryTime);
        }
    }

    private boolean needUpdateToken() {
        if (token == null) {
            return true;
        }
        return System.currentTimeMillis() > expiryTime * 1000L;
    }

    private void initTokenFromDisk() {
        CallUtils.d("initTokenFromDisk() called");
        token = SPStaticUtils.getString(Constants.ZEGO_TOKEN_KEY);
        expiryTime = SPStaticUtils.getLong(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY);
    }

    private void forceUpdateToken() {
        ZegoUserInfo userInfo = ZegoCallManager.getInstance().getLocalUserInfo();
        if (userInfo != null && !TextUtils.isEmpty(userInfo.userID)) {
            this.getToken(userInfo.userID, true, (errorCode, token) -> {

            });
        }
    }
}