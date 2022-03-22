package im.zego.callsdk.command;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.service.ZegoCommandManager;
import java.util.HashMap;
import java.util.Map;

public abstract class ZegoCommand {

    protected String path;
    protected Map<String, Object> parameter;

    public static final String GET_TOKEN = "/user/get_token";
    public static final String LOGIN = "/user/login";
    public static final String LOGOUT = "/user/logout";
    public static final String GET_USER_LIST = " /user/get_users";
    public static final String HEARTBEAT = "/call/heartbeat";
    public static final String START_CALL = "/call/start_call";
    public static final String CANCEL_CALL = "/call/cancel_call";
    public static final String RESPOND_CALL = "/call/respond_call";
    public static final String END_CALL = "/call/end_call";

    public ZegoCommand(String path) {
        this.path = path;
    }


    public void execute(ZegoRequestCallback callback) {
        ZegoCommandManager.getInstance().execute(this, callback);
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public void putParameter(String key, String value) {
        if (parameter == null) {
            parameter = new HashMap<>();
        }
        parameter.put(key, value);
    }
}
