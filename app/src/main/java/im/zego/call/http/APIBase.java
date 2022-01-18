package im.zego.call.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;


public class APIBase {

    private static final String TAG = "APIBase";

    private static Gson mGson = new Gson();
    private static final Handler okHandler = new Handler(Looper.getMainLooper());
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static class OkHttpInstance {

        private volatile static OkHttpInstance instance;
        private OkHttpClient mOkHttpClient;

        private OkHttpInstance() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
            mOkHttpClient = builder.build();
        }

        public static OkHttpClient getInstance() {
            if (instance == null) {
                synchronized (OkHttpInstance.class) {
                    if (instance == null) {
                        instance = new OkHttpInstance();
                    }
                }
            }
            return instance.mOkHttpClient;
        }

    }

    public static <T> void asyncGet(@NotNull String url, final Class<T> classType,
        final IAsyncGetCallback<T> reqCallback) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();

        OkHttpInstance.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                try {
                    String str = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(str).getAsJsonObject();
                    int code = jsonObject.get("code").getAsInt();
                    String message = jsonObject.get("message").getAsString();
                    JsonObject dataJson = jsonObject.get("data").getAsJsonObject();
                    if (reqCallback != null) {
                        okHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                T bean = mGson.fromJson(dataJson, classType);
                                reqCallback.onResponse(code, message, bean);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (reqCallback != null) {
                        okHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                reqCallback
                                    .onResponse(ErrorcodeConstants.ErrorJSONFormatInvalid, e.getMessage(), null);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                if (reqCallback != null) {
                    okHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            reqCallback.onResponse(ErrorcodeConstants.ErrorFailNetwork, "Network exception", null);
                        }
                    });
                }

            }
        });
    }

    public static <T> void asyncPost(String url, String json, final Class<T> classType,
        final IAsyncGetCallback<T> reqCallback) {
        asyncPost(url, json, (errorCode, message, response) -> {
            T bean = mGson.fromJson(response, classType);
            reqCallback.onResponse(errorCode, message, bean);
        });
    }

    public static <T> void asyncPost(String url, String json, final IAsyncGetCallback<JsonObject> reqCallback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        Call call = OkHttpInstance.getInstance().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], e = [" + e + "]");
                if (reqCallback != null) {
                    okHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            reqCallback.onResponse(ErrorcodeConstants.ErrorFailNetwork, e.getMessage(), null);
                        }
                    });
                }

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String str = response.body().string();
                    Log.d(TAG, "post to" + url + ",onResponse: " + str);
                    JsonObject jsonObject = JsonParser.parseString(str).getAsJsonObject();
                    int code = jsonObject.get("code").getAsInt();
                    String message = jsonObject.get("message").getAsString();
                    JsonElement data = jsonObject.get("data");
                    JsonObject dataJson = (data == null) ? null : data.getAsJsonObject();
                    if (reqCallback != null) {
                        JsonObject finalDataJson = dataJson;
                        okHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                reqCallback.onResponse(code, message, finalDataJson);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (reqCallback != null) {
                        okHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                reqCallback
                                    .onResponse(ErrorcodeConstants.ErrorJSONFormatInvalid, e.getMessage(), null);
                            }
                        });
                    }
                }
            }
        });
    }
}
