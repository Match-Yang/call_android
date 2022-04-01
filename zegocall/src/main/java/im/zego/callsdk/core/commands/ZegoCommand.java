package im.zego.callsdk.core.commands;

import java.util.HashMap;
import java.util.Map;

import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.command.ZegoCommandManager;

public class ZegoCommand {

    protected String path;
    protected Map<String, Object> parameter;

    public static final String GET_TOKEN = "/user/get_token";
    public static final String LOGIN = "/user/login";
    public static final String LOGOUT = "/user/logout";
    public static final String GET_USER_LIST = " /user/get_users";
    public static final String HEARTBEAT = "/call/heartbeat";
    public static final String START_CALL = "/call/start_call";
    public static final String CANCEL_CALL = "/call/cancel_call";
    public static final String ACCEPT_CALL = "/call/accept_call";
    public static final String DECLINE_CALL = "/call/decline_call";
    public static final String END_CALL = "/call/end_call";
    public static final String Listener_CALL = "/call/listen_call";

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

    public void putParameter(String key, Object value) {
        if (parameter == null) {
            parameter = new HashMap<>();
        }
        parameter.put(key, value);
    }
}
