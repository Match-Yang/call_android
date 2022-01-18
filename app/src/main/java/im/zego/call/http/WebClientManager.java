package im.zego.call.http;

import android.util.Log;
import androidx.annotation.NonNull;
import im.zego.call.http.bean.UserBean;
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


    public void startHeartBeat(String userID) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                CallApi.heartBeat(userID, null);
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 30 * 1000);
    }

    public void stopHeartBeat() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
