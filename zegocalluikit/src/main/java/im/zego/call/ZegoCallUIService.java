package im.zego.call;

import com.jeremyliao.liveeventbus.LiveEventBus;

import im.zego.call.constant.Constants;
import im.zego.call.ui.call.CallStateManager;
import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.core.manager.ZegoServiceManager;

/**
 * CallKit服务类，可用于用户登录、登出等逻辑调用
 */
public class ZegoCallUIService {

    /**
     * 用户登录
     * 调用时机：在google授权登录之后
     * @param authToken google授权拿到的token
     * @param callback 回调，返回登录成功或失败
     */
    public void login(String authToken, ZegoCallback callback) {
        ZegoServiceManager.getInstance().userService.login(authToken, callback);
    }

    /**
     * 用户登出
     * 调用时机：在登录成功之后
     */
    public void logout() {
        if (CallStateManager.getInstance().isInCallingStream()) {
            LiveEventBus.get(Constants.EVENT_CANCEL_CALL).post(null);
        } else if (CallStateManager.getInstance().isConnected()) {
            LiveEventBus.get(Constants.EVENT_END_CALL).post(null);
        } else {
            CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
        }
        ZegoServiceManager.getInstance().userService.logout();
    }
}