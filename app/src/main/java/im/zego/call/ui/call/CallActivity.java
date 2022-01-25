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
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import im.zego.call.R;
import im.zego.call.auth.AuthInfoManager;
import im.zego.call.databinding.ActivityCallBinding;
import im.zego.call.ui.BaseActivity;
import im.zego.call.ui.call.CallStateManager.CallStateChangedListener;
import im.zego.callsdk.model.ZegoCallType;
import im.zego.callsdk.model.ZegoUserInfo;
import im.zego.callsdk.service.ZegoRoomManager;
import im.zego.callsdk.service.ZegoUserService;
import java.util.Locale;
import java.util.Objects;

public class CallActivity extends BaseActivity<ActivityCallBinding> {

    private static final String TAG = "CallActivity";

    private static final String USER_INFO = "user_info";

    private ZegoUserInfo userInfo;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable cancelCallRunnable = () -> {
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.cancelCallToUser(userInfo.userID, errorCode -> {
            CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_CALL_MISSED);
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

    private long time;
    private CallStateChangedListener callStateChangedListener;

    public static void startCallActivity(ZegoUserInfo userInfo) {
        Log.d(TAG, "startCallActivity() called with: userInfo = [" + userInfo + "]");
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent(topActivity, CallActivity.class);
        intent.putExtra(USER_INFO, userInfo);
        topActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userInfo = (ZegoUserInfo) getIntent().getSerializableExtra(USER_INFO);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
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
        int typeOfCall = CallStateManager.getInstance().getCallState();
        updateUi(typeOfCall);

        initDeviceState(typeOfCall);

        callStateChangedListener = new CallStateChangedListener() {
            @Override
            public void onCallStateChanged(int before, int after) {
                Log.d(TAG, "onCallStateChanged() called with: before = [" + before + "], after = [" + after + "]");
                updateUi(after);
                boolean beforeIsOutgoing = (before == CallStateManager.TYPE_OUTGOING_CALLING_AUDIO) ||
                    (before == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO);
                boolean beforeIsInComing = (before == CallStateManager.TYPE_INCOMING_CALLING_AUDIO) ||
                    (before == CallStateManager.TYPE_INCOMING_CALLING_VIDEO);
                boolean afterIsAccept = (after == CallStateManager.TYPE_CONNECTED_VOICE) ||
                    (after == CallStateManager.TYPE_CONNECTED_VIDEO);
                if ((beforeIsOutgoing || beforeIsInComing) && afterIsAccept) {
                    time = 0;
                    handler.postDelayed(timeCountRunnable, 1000);
                    handler.removeCallbacks(cancelCallRunnable);
                } else if (beforeIsOutgoing && after == CallStateManager.TYPE_CALL_CANCELED) {
                    ToastUtils.showShort(R.string.state_canceled);
                    binding.layoutOutgoingCall.updateStateText(R.string.state_canceled);
                    binding.layoutIncomingCall.updateStateText(R.string.state_canceled);
                    finishActivityDelayed();
                } else if (after == CallStateManager.TYPE_CALL_COMPLETED) {
                    ToastUtils.showShort(R.string.state_complete);
                    finishActivityDelayed();
                } else if (after == CallStateManager.TYPE_CALL_MISSED) {
                    ToastUtils.showShort(R.string.state_missed);
                    binding.layoutOutgoingCall.updateStateText(R.string.state_missed);
                    binding.layoutIncomingCall.updateStateText(R.string.state_missed);
                    finishActivityDelayed();
                } else if (after == CallStateManager.TYPE_CALL_DECLINE) {
                    ToastUtils.showShort(R.string.state_declined);
                    binding.layoutOutgoingCall.updateStateText(R.string.state_declined);
                    binding.layoutIncomingCall.updateStateText(R.string.state_declined);
                    finishActivityDelayed();
                }
            }
        };
        CallStateManager.getInstance().addListener(callStateChangedListener);
    }

    private void initDeviceState(int typeOfCall) {
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        String userID = userService.localUserInfo.userID;
        String token = AuthInfoManager.getInstance().generateCreateRoomToken(userID, userID);
        if (typeOfCall == CallStateManager.TYPE_OUTGOING_CALLING_AUDIO) {
            userService.callToUser(userInfo.userID, ZegoCallType.Audio, token, errorCode -> {
                if (errorCode == 0) {
                    userService.micOperate(true, errorCode1 -> {
                        if (errorCode1 == 0) {
                        } else {
                            ToastUtils.showShort(getString(R.string.mic_operate_failed, errorCode1));
                        }
                    });
                    handler.postDelayed(cancelCallRunnable, 60 * 1000);
                } else {
                    showWarnTips(getString(R.string.call_user_failed, errorCode));
                    finishActivityDelayed();
                }
            });
        } else if (typeOfCall == CallStateManager.TYPE_OUTGOING_CALLING_VIDEO) {
            userService.callToUser(userInfo.userID, ZegoCallType.Video, token, errorCode -> {
                if (errorCode == 0) {
                    TextureView textureView = binding.layoutOutgoingCall.getTextureView();
                    userService.cameraOperate(true, errorCode1 -> {
                        if (errorCode1 == 0) {
                            userService.micOperate(true, errorCode2 -> {
                                if (errorCode2 == 0) {
                                }
                            });
                        } else {
                            ToastUtils.showShort(getString(R.string.camera_operate_failed, errorCode1));
                        }
                        userService.startPlayingUserMedia(userService.localUserInfo.userID, textureView);
                    });
                    handler.postDelayed(cancelCallRunnable, 60 * 1000);
                } else {
                    showWarnTips(getString(R.string.call_user_failed, errorCode));
                    finishActivityDelayed();
                }
            });
        } else if (typeOfCall == CallStateManager.TYPE_INCOMING_CALLING_VIDEO) {
        } else if (typeOfCall == CallStateManager.TYPE_INCOMING_CALLING_AUDIO) {
        } else if (typeOfCall == CallStateManager.TYPE_CONNECTED_VOICE) {
            handler.postDelayed(timeCountRunnable, 1000);
            userService.micOperate(true, errorCode -> {
                if (errorCode == 0) {
                }
            });
        } else if (typeOfCall == CallStateManager.TYPE_CONNECTED_VIDEO) {
            handler.postDelayed(timeCountRunnable, 1000);
            userService.micOperate(true, errorCode -> {
                if (errorCode == 0) {
                    userService.cameraOperate(true, errorCode1 -> {
                        if (errorCode1 == 0) {
                        }
                    });
                }
            });
        }
        userService.speakerOperate(true);
    }

    private void updateUi(int type) {
        binding.layoutOutgoingCall.setUserInfo(userInfo);
        binding.layoutOutgoingCall.setCallType(type);
        binding.layoutIncomingCall.setCallType(type);
        binding.layoutIncomingCall.setUserInfo(userInfo);
        binding.layoutConnectedVoiceCall.setUserInfo(userInfo);
        binding.layoutConnectedVideoCall.setUserInfo(userInfo);

        switch (type) {
            case CallStateManager.TYPE_INCOMING_CALLING_AUDIO:
            case CallStateManager.TYPE_INCOMING_CALLING_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.VISIBLE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutConnectedVideoCall.setVisibility(View.GONE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.GONE);
                break;
            case CallStateManager.TYPE_CONNECTED_VOICE:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutConnectedVideoCall.setVisibility(View.GONE);
                binding.layoutConnectedVoiceCall.setVisibility(View.VISIBLE);
                binding.callTime.setVisibility(View.VISIBLE);
                break;
            case CallStateManager.TYPE_CONNECTED_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.GONE);
                binding.layoutConnectedVideoCall.setVisibility(View.VISIBLE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.VISIBLE);
                break;
            case CallStateManager.TYPE_OUTGOING_CALLING_AUDIO:
            case CallStateManager.TYPE_OUTGOING_CALLING_VIDEO:
                binding.layoutIncomingCall.setVisibility(View.GONE);
                binding.layoutOutgoingCall.setVisibility(View.VISIBLE);
                binding.layoutConnectedVideoCall.setVisibility(View.GONE);
                binding.layoutConnectedVoiceCall.setVisibility(View.GONE);
                binding.callTime.setVisibility(View.GONE);
                break;
        }
    }

    private void finishActivityDelayed() {
        handler.postDelayed(() -> {
            finish();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        CallStateManager.getInstance().setCallState(userInfo, CallStateManager.TYPE_NO_CALL);
        CallStateManager.getInstance().removeListener(callStateChangedListener);
    }

    @Override
    public void onBackPressed() {
    }

    public void onUserInfoUpdated(ZegoUserInfo userInfo) {
        if (Objects.equals(this.userInfo, userInfo)) {
            this.userInfo = userInfo;
        }
        binding.layoutIncomingCall.onUserInfoUpdated(userInfo);
        binding.layoutOutgoingCall.onUserInfoUpdated(userInfo);
        binding.layoutConnectedVideoCall.onUserInfoUpdated(userInfo);
        binding.layoutConnectedVoiceCall.onUserInfoUpdated(userInfo);
    }
}