package im.zego.call.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.blankj.utilcode.util.StringUtils;

import im.zego.call.R;
import im.zego.call.ui.model.VideoSettingConfig;
import im.zego.callsdk.model.ZegoAudioBitrate;
import im.zego.callsdk.model.ZegoDevicesType;
import im.zego.callsdk.model.ZegoVideoResolution;
import im.zego.callsdk.service.ZegoDeviceService;
import im.zego.callsdk.service.ZegoServiceManager;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;

public class VideoConfigViewModel extends ViewModel {

    public final String[] videoResolutionStringArray = StringUtils.getStringArray(R.array.video_resolution);
    public final String[] audioBitrateStringArray = StringUtils.getStringArray(R.array.audio_bitrate);

    private final VideoSettingConfig settingConfig = new VideoSettingConfig();

    public VideoSettingConfig getSettingConfig() {
        return settingConfig;
    }

    public void init() {
        settingConfig.setBackgroundNoiseReduction(false);
        settingConfig.setEchoCancellation(false);
        settingConfig.setMicVolumeAutoAdjustment(false);
        settingConfig.setMirroring(false);
        settingConfig.setVideoResolution(videoResolutionStringArray[1]);
        settingConfig.setAudioBitrate(audioBitrateStringArray[0]);
    }

    public void updateVideoConfig() {
        ZegoDeviceService deviceService = ZegoServiceManager.getInstance().deviceService;

        int index = 0;
        for (int i = 0; i < ZegoVideoConfigPreset.values().length; i++) {
            String enumName = ZegoVideoConfigPreset.values()[i].name();
            if (settingConfig.getVideoResolution().contains(enumName.replaceAll("\\D+", ""))) {
                index = i;
                break;
            }
        }
        deviceService.setVideoResolution(ZegoVideoResolution.getVideoResolution(index));

        int audioBitrate = VideoSettingConfig.calculateAudioBitrate(settingConfig.getAudioBitrate());
        deviceService.setAudioBitrate(ZegoAudioBitrate.getAudioBitrate(audioBitrate));

        deviceService.setDeviceStatus(ZegoDevicesType.NOISE_SUPPRESSION, settingConfig.isBackgroundNoiseReduction());

        deviceService.setDeviceStatus(ZegoDevicesType.ECHO_CANCELLATION, settingConfig.isEchoCancellation());

        deviceService.setDeviceStatus(ZegoDevicesType.VOLUME_ADJUSTMENT, settingConfig.isMicVolumeAutoAdjustment());
    }
}