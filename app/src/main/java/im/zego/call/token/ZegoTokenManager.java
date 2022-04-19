package im.zego.call.token;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPStaticUtils;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.callback.ZegoTokenCallback;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.utils.CallUtils;
import im.zego.callsdk.utils.ZegoCallErrorCode;
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
                ZegoUserInfo userInfo = ZegoCallManager.getInstance().getLocalUserInfo();
                if (userInfo == null) {
                    return;
                }
                boolean updateToken = needUpdateToken(userInfo.userID);
                if (updateToken) {
                    forceUpdateToken();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 60_000);
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

    private static final long EFFECTIVE_TIME_MILLS = 24 * 3600L * 1000;

    private String currentToken;
    private long currentTokenExpiryTime;
    private String currentUserID;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
        Locale.getDefault());

    public void getToken(@NonNull String userID, boolean isForceUpdate, @NonNull ZegoTokenCallback callback) {
        CallUtils.d("getToken() called with: userID = [" + userID + "], isForceUpdate = [" + isForceUpdate + "]");
        if (!isForceUpdate && !needUpdateToken(userID)) {
            callback.onTokenCallback(ZegoExpressErrorCode.CommonSuccess, this.currentToken);
            return;
        }

        this.getTokenFromServer(userID, EFFECTIVE_TIME_MILLS / 1000, (errorCode, token) -> {
            if (errorCode == ZegoExpressErrorCode.CommonSuccess) {
                this.currentToken = token;
                this.currentUserID = userID;
                this.currentTokenExpiryTime = System.currentTimeMillis() + EFFECTIVE_TIME_MILLS;
                saveToken(this.currentToken, currentTokenExpiryTime, this.currentUserID);
                callback.onTokenCallback(errorCode, token);
            } else {
                callback.onTokenCallback(errorCode, null);
            }
        });
    }

    public void reset() {
        saveToken(null, 0, "");
    }

    private void getTokenFromServer(@NonNull String userID, long effectiveTime, @NonNull ZegoTokenCallback callback) {
        getTokenFromCloudFunction(userID, effectiveTime, new ZegoRequestCallback() {
            @Override
            public void onResult(int errorCode, Object obj) {
                CallUtils.d("getToken onResult() called with: errorCode = [" + errorCode + "], obj = [" + obj + "]");
                callback.onTokenCallback(errorCode, (String) obj);
            }
        });
    }

    private void saveToken(String token, long expiryTime, String userID) {
        CallUtils.d("saveToken() called with: token = [" + token + "], expiryTime = ["
            + simpleDateFormat.format(new Date(expiryTime)) + "]");
        if (token == null || expiryTime == 0 || TextUtils.isEmpty(userID)) {
            SPStaticUtils.remove(Constants.ZEGO_TOKEN_KEY);
            SPStaticUtils.remove(Constants.ZEGO_TOKEN_UID);
            SPStaticUtils.remove(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY);
            this.currentTokenExpiryTime = 0;
            this.currentToken = null;
            this.currentUserID = null;
        } else {
            SPStaticUtils.put(Constants.ZEGO_TOKEN_KEY, token);
            SPStaticUtils.put(Constants.ZEGO_TOKEN_UID, userID);
            SPStaticUtils.put(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY, expiryTime);
        }
    }

    private boolean needUpdateToken(String userID) {
        CallUtils.d("currentUserID:" + currentUserID
            + ",currentTokenExpiryTime : " + (simpleDateFormat.format(new Date(currentTokenExpiryTime)))
            + ",currentToken: " + currentToken);
        if (!TextUtils.isEmpty(this.currentUserID) && Objects.equals(this.currentUserID, userID)
            && !TextUtils.isEmpty(this.currentToken) && (currentTokenExpiryTime > System.currentTimeMillis())) {
            return false;
        }
        return true;
    }

    private void initTokenFromDisk() {
        currentToken = SPStaticUtils.getString(Constants.ZEGO_TOKEN_KEY);
        currentTokenExpiryTime = SPStaticUtils.getLong(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY);
        currentUserID = SPStaticUtils.getString(Constants.ZEGO_TOKEN_UID);
        CallUtils.d("initTokenFromDisk() called,currentUserID:" + currentUserID + ",currentTokenExpiryTime:"
            + simpleDateFormat.format(new Date(currentTokenExpiryTime)) + ",currentToken:" + currentToken);
    }

    private void forceUpdateToken() {
        ZegoUserInfo userInfo = ZegoCallManager.getInstance().getLocalUserInfo();
        if (userInfo != null && !TextUtils.isEmpty(userInfo.userID)) {
            this.getToken(userInfo.userID, true, (errorCode, token) -> {

            });
        }
    }

    private void getTokenFromCloudFunction(String userID, long effectiveTime, ZegoRequestCallback callback) {
        CallUtils.d(
            "getTokenFromCloudFunction() called with: userID = [" + userID + "], effectiveTime = [" + effectiveTime
                + "], callback = [" + callback + "]");
        Map<String, Object> data = new HashMap<>();
        data.put("id", userID);
        data.put("effective_time", effectiveTime);

        FirebaseFunctions.getInstance().getHttpsCallable("getToken")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, Object>() {
                @Override
                public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    return task.getResult().getData();
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                        }
                        if (callback != null) {
                            callback.onResult(ZegoCallErrorCode.ZegoErrorNetworkError, e.getMessage());
                        }
                        return;
                    }
                    HashMap<String, String> result = (HashMap<String, String>) task.getResult();
                    if (callback != null) {
                        callback.onResult(0, result.get("token"));
                    }
                }
            });
    }
}