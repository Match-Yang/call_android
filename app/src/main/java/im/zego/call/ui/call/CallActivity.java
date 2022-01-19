package im.zego.call.ui.call;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.gyf.immersionbar.ImmersionBar;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.databinding.ActivityCallBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.view.ConnectedVoiceCallView.ConnectedVoiceCallLister;
import im.zego.call.ui.call.view.OutgoingCallView.OutgoingCallLister;
import im.zego.callsdk.listener.ZegoUserServiceListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoResponseType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.Locale;

public class CallActivity extends BaseActivity<ActivityCallBinding> {

    private static final String TAG = "CallActivity";

    public static final int TYPE_INCOMING_CALLING_VOICE = 0;
    public static final int TYPE_INCOMING_CALLING_VIDEO = 1;
    public static final int TYPE_CONNECTED_VOICE = 2;
    public static final int TYPE_CONNECTED_VIDEO = 3;
    public static final int TYPE_OUTGOING_CALLING_VOICE = 4;
    public static final int TYPE_OUTGOING_CALLING_VIDEO = 5;
    private static final String CALL_TYPE = "call_type";
    private static final String USER_INFO = "user_info";

    private int typeOfCall;
    private ZegoUserInfo userInfo;
    private Runnable cancelCallRunnable = () -> {
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.cancelCallToUser(userInfo.userID, errorCode -> {
            finish();
        });
    };
    private Runnable timeCountRunnable = new Runnable() {
        @Override
        public void run() {
            time++;
            String timeFormat = String.format(Locale.getDefault(), "%02d:%02d", time / 60, time % 60);
            binding.callTime.setText(timeFormat);
            handler.postDelayed(timeCountRunnable, 1000);
        }
    };
    private ZegoUserServiceListener userServiceListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private long time;

    public static void startCallActivity(int type, ZegoUserInfo userInfo) {
        Log.d(TAG, "startCallActivity() called with: type = [" + type + "], userInfo = [" + userInfo + "]");
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent(topActivity, CallActivity.class);
        intent.putExtra(CALL_TYPE, type);
        intent.putExtra(USER_INFO, userInfo);
        topActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        typeOfCall = getIntent().getIntExtra(CALL_TYPE, 0);
        userInfo = (ZegoUserInfo) getIntent().getSerializableExtra(USER_INFO);
        Log.d(TAG,
            "onCreate() called with: Build.VERSION.SDK_INT = [" + Build.VERSION.SDK_INT + "],type:" + typeOfCall);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

            keyguardManager.requestDismissKeyguard(this, new KeyguardDismissCallback() {
                @Override
                public void onDismissError() {
                    super.onDismissError();
                    Log.d(TAG, "onDismissError() called");
                }

                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    Log.d(TAG, "onDismissSucceeded() called");
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                    Log.d(TAG, "onDismissCancelled() called");
                }
            });
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).reset().init();

        initView();
    }

    private void initView() {
        updateUi(typeOfCall);

        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        String userID = userService.localUserInfo.userID;
        String token = AuthInfoManager.getInstance().generateCreateRoomToken(userID, userID);
        if (typeOfCall == CallActivity.TYPE_OUTGOING_CALLING_VOICE) {
            userService.callToUser(userInfo.userID, ZegoCallType.Audio, token, errorCode -> {
                if (errorCode == 0) {
                    handler.postDelayed(cancelCallRunnable, 60 * 1000);
                } else {
                    showWarnTips("Failed to call,errorCode :" + errorCode);
                    handler.postDelayed(cancelCallRunnable,1000);
                }
            });
        } else if (typeOfCall == CallActivity.TYPE_OUTGOING_CALLING_VIDEO) {
            userService.callToUser(userInfo.userID, ZegoCallType.Video, token, errorCode -> {
                if (errorCode == 0) {
                    handler.postDelayed(cancelCallRunnable, 60 * 1000);
                } else {
                    showWarnTips("Failed to call,errorCode :" + errorCode);
                    handler.postDelayed(cancelCallRunnable,1000);
                }
            });
        }

        binding.layoutOutgoingCall.setLister(new OutgoingCallLister() {
            @Override
            public void onCancelCall(int errorCode) {
                finish();
            }
        });
        binding.layoutConnectedVoiceCall.setListener(new ConnectedVoiceCallLister() {
            @Override
            public void onEndCall(int errorCode) {
                finish();
            }
        });

        userServiceListener = new ZegoUserServiceListener() {
            @Override
            public void onUserInfoUpdated(ZegoUserInfo userInfo) {

            }

            @Override
            public void onCallReceived(ZegoUserInfo userInfo, ZegoCallType type) {

            }

            @Override
            public void onCancelCallReceived(ZegoUserInfo userInfo) {

            }

            @Override
            public void onCallResponseReceived(ZegoUserInfo userInfo, ZegoResponseType type) {
                Log.d(TAG,
                    "onCallResponseReceived() called with: userInfo = [" + userInfo + "], type = [" + type + "]");
                if (type == ZegoResponseType.Decline) {
                    handler.removeCallbacks(cancelCallRunnable);
                    handler.post(cancelCallRunnable);
                } else {
                    handler.removeCallbacks(cancelCallRunnable);
                    if (typeOfCall == CallActivity.TYPE_OUTGOING_CALLING_VOICE) {
                        typeOfCall = CallActivity.TYPE_CONNECTED_VOICE;
                        time = 0;
                        updateUi(typeOfCall);
                        handler.postDelayed(timeCountRunnable, 1000);
                    } else if (typeOfCall == CallActivity.TYPE_OUTGOING_CALLING_VIDEO) {
                        typeOfCall = CallActivity.TYPE_CONNECTED_VIDEO;
                        time = 0;
                        updateUi(typeOfCall);
                        handler.postDelayed(timeCountRunnable, 1000);
                    }
                }
            }

            @Override
            public void onEndCallReceived() {
                ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                userService.endCall(errorCode -> {
                    finish();
                });
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {

            }
        };

        if (typeOfCall == TYPE_CONNECTED_VOICE || typeOfCall == TYPE_CONNECTED_VIDEO) {
            handler.postDelayed(timeCountRunnable, 1000);
        }
    }

    private void updateUi(int type) {
        binding.layoutOutgoingCall.setUserInfo(userInfo);
        binding.layoutIncomingCall.setUserInfo(userInfo);
        binding.layoutConnectedVoiceCall.setUserInfo(userInfo);
        binding.layoutConnectedVideoCall.setUserInfo(userInfo);

        switch (type) {
            case TYPE_INCOMING_CALLING_VOICE:
            case TYPE_INCOMING_CALLING_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.VISIBLE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutIncomingCall.updateUi(type == TYPE_INCOMING_CALLING_VIDEO);
                binding.layoutConnectedVideoCall.setVisibility(View.GONE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.GONE);
                break;
            case TYPE_CONNECTED_VOICE:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutConnectedVideoCall.setVisibility(View.GONE);
                binding.layoutConnectedVoiceCall.setVisibility(View.VISIBLE);
                binding.callTime.setVisibility(View.VISIBLE);
                break;
            case TYPE_CONNECTED_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutConnectedVideoCall.setVisibility(View.VISIBLE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.VISIBLE);
                break;
            case TYPE_OUTGOING_CALLING_VOICE:
            case TYPE_OUTGOING_CALLING_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.VISIBLE);
                binding.layoutConnectedVideoCall.setVisibility(View.VISIBLE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(userServiceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
    }
}