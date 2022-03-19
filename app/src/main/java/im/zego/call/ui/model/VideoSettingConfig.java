package im.zego.call.ui.model;

public class VideoSettingConfig {
    private String videoResolution;
    private String audioBitrate;
    private boolean backgroundNoiseReduction;
    private boolean echoCancellation;
    private boolean micVolumeAutoAdjustment;
    private boolean mirroring;

    public boolean isMirroring() {
        return mirroring;
    }

    public void setMirroring(boolean mirroring) {
        this.mirroring = mirroring;
    }

    public String getVideoResolution() {
        return videoResolution;
    }

    public void setVideoResolution(String videoResolution) {
        this.videoResolution = videoResolution;
    }

    public String getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(String audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public static int calculateAudioBitrate(String audioBitrate) {
        return Integer.parseInt(audioBitrate.replace("kbps", ""));
    }

    public boolean isBackgroundNoiseReduction() {
        return backgroundNoiseReduction;
    }

    public void setBackgroundNoiseReduction(boolean backgroundNoiseReduction) {
        this.backgroundNoiseReduction = backgroundNoiseReduction;
    }

    public boolean isEchoCancellation() {
        return echoCancellation;
    }

    public void setEchoCancellation(boolean echoCancellation) {
        this.echoCancellation = echoCancellation;
    }

    public boolean isMicVolumeAutoAdjustment() {
        return micVolumeAutoAdjustment;
    }

    public void setMicVolumeAutoAdjustment(boolean micVolumeAutoAdjustment) {
        this.micVolumeAutoAdjustment = micVolumeAutoAdjustment;
    }

    @Override
    public String toString() {
        return "VideoSettingConfig{" +
                "videoResolution='" + videoResolution + '\'' +
                ", audioBitrate='" + audioBitrate + '\'' +
                ", backgroundNoiseReduction=" + backgroundNoiseReduction +
                ", echoCancellation=" + echoCancellation +
                ", micVolumeAutoAdjustment=" + micVolumeAutoAdjustment +
                ", mirroring=" + mirroring +
                '}';
    }
}