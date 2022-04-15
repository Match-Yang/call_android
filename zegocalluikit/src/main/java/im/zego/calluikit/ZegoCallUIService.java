package im.zego.calluikit;

import im.zego.calluikit.ui.call.CallStateManager;

/**
 * CallKit服务类，可用于用户登录、登出等逻辑调用
 */
public class ZegoCallUIService {


    /**
     * 用户登出 调用时机：在登录成功之后
     */
    public void logout() {
        //        if (CallStateManager.getInstance().isInCallingStream()) {
        //            LiveEventBus.get(Constants.EVENT_CANCEL_CALL).post(null);
        //        } else if (CallStateManager.getInstance().isConnected()) {
        //            LiveEventBus.get(Constants.EVENT_END_CALL).post(null);
        //        } else {
        CallStateManager.getInstance().setCallState(null, CallStateManager.TYPE_NO_CALL);
    }
}