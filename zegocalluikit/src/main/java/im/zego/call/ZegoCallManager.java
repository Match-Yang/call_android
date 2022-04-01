package im.zego.call;

import android.app.Activity;
import android.app.Application;

import im.zego.callsdk.callback.ZegoCallback;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.listener.ZegoCallServiceListener;
import im.zego.callsdk.model.ZegoUserInfo;

/**
 * ZegoCall UIKit管理类 Demo层只需调用并关注此类的实现，即可快速实现一套呼叫对讲业务逻辑
 */
public class ZegoCallManager {

    private static final String TAG = "ZegoCallManager";

    private static volatile ZegoCallManager singleton = null;

    private ZegoCallManager() {
        callKitService = new ZegoCallUIService();
        impl = new ZegoCallManagerImpl();
    }

    public static ZegoCallManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoCallManager.class) {
                if (singleton == null) {
                    singleton = new ZegoCallManager();
                }
            }
        }
        return singleton;
    }

    // CallKit服务类
    public final ZegoCallUIService callKitService;
    private ZegoCallManagerImpl impl;

    /**
     * 初始化sdk与rtc引擎 调用时机：应用启动时
     */
    public void init(Application application) {
        impl.init(application);
    }

    public void setListener(ZegoCallServiceListener listener) {
        impl.setListener(listener);
    }

    /**
     * 启动监听呼叫响应 调用时机：成功登录之后
     */
    public void startListen(Activity activity) {
        impl.startListen(activity);
    }

    /**
     * 停止监听呼叫响应 调用时机：退出登录之后
     */
    public void stopListen(Activity activity) {
        impl.stopListen(activity);
    }

    /**
     * 上传日志
     *
     * @param callback
     */
    public void uploadLog(final ZegoCallback callback) {
        impl.uploadLog(callback);
    }

    /**
     * 主动呼叫用户
     *
     * @param userInfo  用户信息
     * @param callState 呼叫类型，语音/视频
     */
    public void callUser(ZegoUserInfo userInfo, int callState) {
        impl.callUser(userInfo, callState);
    }

    /**
     * 获取本地用户信息
     */
    public ZegoUserInfo getLocalUserInfo() {
        return impl.getLocalUserInfo();
    }

    public void getToken(String userID, long effectiveTime, ZegoRequestCallback callback) {
        impl.getToken(userID, effectiveTime, callback);
    }

    /**
     * 展示前台服务通知 调用时机：应用切换到后台后
     */
    public void showNotification(ZegoUserInfo userInfo) {
        impl.showNotification(userInfo);
    }

    /**
     * 隐藏前台服务通知 调用时机：应用切换到前台后
     */
    public void dismissNotification(Activity activity) {
        impl.dismissNotification(activity);
    }
}
