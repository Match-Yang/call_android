package im.zego.calluikit.ui.dialog;

import android.content.Context;
import android.view.View;

import com.blankj.utilcode.util.StringUtils;

import im.zego.calluikit.R;
import im.zego.calluikit.ui.dialog.base.BaseBottomDialog;
import im.zego.calluikit.ui.viewmodel.VideoConfigViewModel;
import im.zego.calluikit.view.VideoSettingCellView;


public class VideoSettingsDialog extends BaseBottomDialog {

    private VideoSettingCellView backgroundNoiseReduction;
    private VideoSettingCellView echoCancellation;
    private VideoSettingCellView micVolumeAutoAdjustment;
    private VideoSettingCellView mirroring;
    private VideoSettingCellView settingsVideoResolution;
    private VideoSettingCellView settingsAudioBitrate;

    private final VideoConfigViewModel viewModel;
    private boolean isVideoCall = false;

    public VideoSettingsDialog(Context context, VideoConfigViewModel viewModel) {
        super(context);
        this.viewModel = viewModel;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_video_settings;
    }

    @Override
    protected void initView() {
        super.initView();
        backgroundNoiseReduction = findViewById(R.id.settings_background_noise_reduction);
        echoCancellation = findViewById(R.id.settings_echo_cancellation);
        micVolumeAutoAdjustment = findViewById(R.id.settings_mic_volume_auto_adjustment);
        mirroring = findViewById(R.id.settings_mirroring);
        settingsVideoResolution = findViewById(R.id.settings_resolution_settings);
        settingsAudioBitrate = findViewById(R.id.settings_audio_bitrate);

        mirroring.setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
        settingsVideoResolution.setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
    }

    public void setIsVideoCall(boolean isVideoCall) {
        this.isVideoCall = isVideoCall;
    }

    @Override
    protected void initData() {
        super.initData();
        backgroundNoiseReduction.setChecked(viewModel.getSettingConfig().isBackgroundNoiseReduction());
        echoCancellation.setChecked(viewModel.getSettingConfig().isEchoCancellation());
        micVolumeAutoAdjustment.setChecked(viewModel.getSettingConfig().isMicVolumeAutoAdjustment());
        mirroring.setChecked(viewModel.getSettingConfig().isMirroring());
        settingsVideoResolution.setContent(viewModel.getSettingConfig().getVideoResolution());
        settingsAudioBitrate.setContent(viewModel.getSettingConfig().getAudioBitrate());
    }

    @Override
    protected void initListener() {
        super.initListener();

        backgroundNoiseReduction.setListener(isChecked -> viewModel.getSettingConfig().setBackgroundNoiseReduction(isChecked));

        echoCancellation.setListener(isChecked -> viewModel.getSettingConfig().setEchoCancellation(isChecked));

        micVolumeAutoAdjustment.setListener(isChecked -> viewModel.getSettingConfig().setMicVolumeAutoAdjustment(isChecked));

        mirroring.setListener(isChecked -> viewModel.getSettingConfig().setMirroring(isChecked));

        settingsVideoResolution.setListener(isChecked -> {
            CommonStringArrayDialog dialog = new CommonStringArrayDialog(
                    getContext(),
                    StringUtils.getString(R.string.room_settings_page_video_resolution),
                    viewModel.getSettingConfig().getVideoResolution(),
                    viewModel.videoResolutionStringArray,
                    checkedString -> {
                        settingsVideoResolution.setContent(checkedString);
                        viewModel.getSettingConfig().setVideoResolution(checkedString);
                    }
            );
            dialog.setOnDismissListener(d -> this.show());
            this.hide();
            dialog.show();
        });

        settingsAudioBitrate.setListener(isChecked -> {
            CommonStringArrayDialog dialog = new CommonStringArrayDialog(
                    getContext(),
                    StringUtils.getString(R.string.room_settings_page_audio_bitrate),
                    viewModel.getSettingConfig().getAudioBitrate(),
                    viewModel.audioBitrateStringArray,
                    checkedString -> {
                        settingsAudioBitrate.setContent(checkedString);
                        viewModel.getSettingConfig().setAudioBitrate(checkedString);
                    }
            );
            dialog.setOnDismissListener(d -> this.show());
            this.hide();
            dialog.show();
        });
    }

    @Override
    public void dismiss() {
        viewModel.updateVideoConfig();
        super.dismiss();
    }
}
