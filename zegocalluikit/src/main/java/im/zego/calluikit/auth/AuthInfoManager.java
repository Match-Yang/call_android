package im.zego.calluikit.auth;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import im.zego.callsdk.auth.TokenServerAssistant;

public class AuthInfoManager {

    private AuthInfoManager() {
    }

    private static final class Holder {

        private static final AuthInfoManager INSTANCE = new AuthInfoManager();
    }

    public static AuthInfoManager getInstance() {
        return Holder.INSTANCE;
    }


    private String serverSecret;
    private long appID;
    private String appSign;

    private static final String TAG = "AuthInfoManager";

    public long getAppID() {
        return appID;
    }

    public void init(Context context) {
        String fileJson = readJsonFile(context, "KeyCenter.json");
        if (fileJson == null || fileJson.isEmpty()) {
            ToastUtils.showLong("please check if \"KeyCenter.json\" file is existed.");
            Log.e(TAG,"please check if \"KeyCenter.json\" file is existed.You can follow ReadMe.md's"
                + "instruction to generate it.");
            return;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(fileJson);
            appID = jsonObject.getLong("appID");
            serverSecret = jsonObject.getString("serverSecret");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "init() called with: appID = [" + appID
            + "]" + ",serverSecret:" + serverSecret);
    }

    private String readJsonFile(Context context, String fileName) {
        String jsonStr = "";
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            inputStream.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateToken(String userID) {
        try {
            return TokenServerAssistant.generateToken(appID, userID, serverSecret, 60 * 60 * 24).data;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}