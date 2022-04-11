package im.zego.call.token;

import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import java.util.Objects;

import im.zego.call.auth.AuthInfoManager;
import im.zego.zegoexpress.ZegoExpressErrorCode;


/**
 * Created by rocket_wang on 2022/4/9.
 */
public class ZegoTokenManager {
    private static volatile ZegoTokenManager singleton = null;

    private ZegoTokenManager() {
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

    private static final long EFFECTIVE_TIME_IN_MILLIS = DateUtils.DAY_IN_MILLIS;

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

        this.getTokenFromServer(userID, EFFECTIVE_TIME_IN_MILLIS, (errorCode, token) -> {
            if (errorCode == ZegoExpressErrorCode.CommonSuccess) {
                this.token = token;
                this.userID = userID;
                this.expiryTime = System.currentTimeMillis() + EFFECTIVE_TIME_IN_MILLIS;
                callback.onTokenCallback(errorCode, token);
            } else {
                callback.onTokenCallback(errorCode, null);
            }
        });
    }

    private void getTokenFromServer(@NonNull String userID, long effectiveTimeInMillis, @NonNull ZegoTokenCallback callback) {
        String token = AuthInfoManager.getInstance().generateToken(userID);
        callback.onTokenCallback(ZegoExpressErrorCode.CommonSuccess, token);
    }
}
