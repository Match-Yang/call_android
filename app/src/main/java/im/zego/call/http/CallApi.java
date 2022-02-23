package im.zego.call.http;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import im.zego.call.http.bean.UserBean;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CallApi {

    private static final String TAG = "CallApi";
    private static final String baseUrl = "https://demo-server-api.zegocloud.com";
    private static Gson gson = new Gson();

    public static final int PARAM_ERROR = 4;
    public static final int USER_OFFLINE_1 = 80001;
    public static final int USER_OFFLINE_2 = 80002;
    public static final int SYSTEM_ERROR = 100000;

    public static void createUser(IAsyncGetCallback<String> reqCallback) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("v1/user/create_user");
        String url = builder.build().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", 1);
        String json = jsonObject.toString();
        APIBase.asyncPost(url, json, (errorCode, message, response) -> {
            String string = "";
            if (response != null && response.get("id") != null) {
                JsonElement element = response.get("id");
                string = element.getAsString();
                if (reqCallback != null) {
                    reqCallback.onResponse(errorCode, message, string);
                }
            } else {
                if (reqCallback != null) {
                    reqCallback.onResponse(ErrorcodeConstants.ErrorJSONFormatInvalid, message, string);
                }
            }
        });
    }

    public static void getUserList(int pageNum, String from, int direct, IAsyncGetCallback<List<UserBean>> callback) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("v1/user/get_user_list");
        String url = builder.build().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("page_num", pageNum);
        jsonObject.addProperty("from", from);
        jsonObject.addProperty("direct", direct);
        jsonObject.addProperty("type", 1);
        String json = jsonObject.toString();
        APIBase.asyncPost(url, json, (errorCode, message, response) -> {
            if (errorCode == 0) {
                if (response != null && response.get("user_list") != null) {
                    JsonElement element = response.get("user_list");
                    JsonArray userArray = element.getAsJsonArray();
                    Type userListType = new TypeToken<ArrayList<UserBean>>() {
                    }.getType();
                    ArrayList<UserBean> userList = gson.fromJson(userArray, userListType);
                    if (callback != null) {
                        callback.onResponse(errorCode, message, userList);
                    }
                } else {
                    if (callback != null) {
                        callback.onResponse(ErrorcodeConstants.ErrorJSONFormatInvalid, message, null);
                    }
                }
            } else {
                if (callback != null) {
                    callback.onResponse(errorCode, message, null);
                }
            }
        });
    }

    public static void login(String name, String userID, IAsyncGetCallback<UserBean> callback) {
        Log.d(TAG, "login() called with: name = [" + name + "], id = [" + userID + "], callback = [" + callback + "]");
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("v1/user/login");
        String url = builder.build().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("id", userID);
        jsonObject.addProperty("type", 1);
        String json = jsonObject.toString();

        APIBase.asyncPost(url, json, (errorCode, message, response) -> {
            if (response != null) {
                UserBean userBean = gson.fromJson(response, UserBean.class);
                if (callback != null) {
                    callback.onResponse(errorCode, message, userBean);
                }
            } else {
                if (callback != null) {
                    callback.onResponse(errorCode, message, null);
                }
            }
        });
    }

    public static void logout(String userID, IAsyncGetCallback<String> callback) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("v1/user/logout");
        String url = builder.build().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", userID);
        jsonObject.addProperty("type", 1);
        String json = jsonObject.toString();

        APIBase.asyncPost(url, json, (errorCode, message, response) -> {
            if (errorCode == 0) {
                WebClientManager.getInstance().stopHeartBeat();
            }
            if (callback != null) {
                callback.onResponse(errorCode, message, "");
            }
        });
    }

    public static void heartBeat(String userID, IAsyncGetCallback<String> callback) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("v1/user/heartbeat");
        String url = builder.build().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", userID);
        jsonObject.addProperty("type", 1);
        String json = jsonObject.toString();

        APIBase.asyncPost(url, json, (errorCode, message, response) -> {
            if (callback != null) {
                callback.onResponse(errorCode, message, "");
            }
        });
    }
}
