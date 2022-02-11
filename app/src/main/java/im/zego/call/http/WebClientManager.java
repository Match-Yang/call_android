package im.zego.call.http;

import android.util.Log;
import androidx.annotation.NonNull;
import im.zego.call.http.bean.UserBean;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebClientManager {

    private WebClientManager() {
    }

    private static final class Holder {

        private static final WebClientManager INSTANCE = new WebClientManager();
    }

    public static WebClientManager getInstance() {
        return Holder.INSTANCE;
    }

    private Timer timer = new Timer();
    private boolean haveMore = false;
    List<UserBean> userList = new ArrayList<>();
    final int pullPerCount = 100;
    private static final String TAG = "WebClientManager";
    private boolean hasLoggedin = false;

    public void getUserList(IAsyncGetCallback<List<UserBean>> callback) {
        userList.clear();
        haveMore = false;
        getUserListInner(null, callback);
    }

    private void getUserListInner(String order, IAsyncGetCallback<List<UserBean>> callback) {
        CallApi.getUserList(pullPerCount, order, 1, new IAsyncGetCallback<List<UserBean>>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, List<UserBean> response) {
                if (errorCode == 0) {
                    userList.addAll(response);
                    haveMore = response.size() == pullPerCount;
                    Log.d(TAG, "onResponse,haveMore: " + haveMore);
                    UserBean userBean = response.get(response.size() - 1);
                    if (haveMore) {
                        getUserListInner(userBean.order, callback);
                    } else {
                        if (callback != null) {
                            callback.onResponse(errorCode, message, userList);
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onResponse(errorCode, message, userList);
                    }
                }

            }
        });
    }

    /**
     * login to web server,make self visible to other online users.
     *
     * @param name     name
     * @param userID   id
     * @param callback result
     */
    public void login(String name, String userID, IAsyncGetCallback<UserBean> callback) {
        Log.d(TAG,
            "login() called with: name = [" + name + "], userID = [" + userID + "], callback = [" + callback + "]");
        CallApi.login(name, userID, new IAsyncGetCallback<UserBean>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, UserBean response) {
                hasLoggedin = errorCode == 0;
                if (errorCode == 0) {
                    WebClientManager.getInstance().startHeartBeat(userID);
                } else {
                    WebClientManager.getInstance().stopHeartBeat();
                }
                if (callback != null) {
                    callback.onResponse(errorCode, message, response);
                }
            }
        });
    }

    /**
     * logout from web server,make self invisible to other online users.
     *
     * @param userID   user id
     * @param callback result
     */
    public void logout(String userID, IAsyncGetCallback<String> callback) {
        Log.d(TAG, "logout() called with: userID = [" + userID + "], callback = [" + callback + "]");
        CallApi.logout(userID, callback);
        hasLoggedin = false;
    }

    public void tryReLogin(IAsyncGetCallback<UserBean> callback) {
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        ZegoUserInfo localUserInfo = userService.localUserInfo;
        CallApi.heartBeat(localUserInfo.userID, new IAsyncGetCallback<String>() {
            @Override
            public void onResponse(int errorCode, @NonNull String message, String response) {
                if (errorCode != 0) {
                    // means heart failed,relogin to make it success,and online for other users
                    CallApi.login(localUserInfo.userName, localUserInfo.userID,
                        new IAsyncGetCallback<UserBean>() {
                            @Override
                            public void onResponse(int errorCode, @NonNull String message,
                                UserBean response) {
                                if (callback != null) {
                                    callback.onResponse(errorCode, message, response);
                                }
                            }
                        });
                }
            }
        });
    }

    /**
     * keep heart to keep self online state.
     *
     * @param userID
     */
    public void startHeartBeat(String userID) {
        Log.d(TAG, "startHeartBeat() called with: userID = [" + userID + "]");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "TimerTask run() called, heartBeat");
                CallApi.heartBeat(userID, new IAsyncGetCallback<String>() {
                    @Override
                    public void onResponse(int errorCode, @NonNull String message, String response) {
                        // if user not logout manually,try login when heartbeat failed
                        if (hasLoggedin && errorCode != 0) {
                            tryReLogin(null);
                        }
                    }
                });
            }
        };
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(task, 0, 15 * 1000);
    }

    public void stopHeartBeat() {
        Log.d(TAG, "stopHeartBeat() called");
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }
}
