package im.zego.calluikit.utils;

import android.widget.ImageView;

import com.blankj.utilcode.util.ResourceUtils;

import im.zego.calluikit.R;
import im.zego.zegoexpress.constants.ZegoAudioRoute;

public class AudioHelper {
    public static void updateAudioSelect(ImageView imageView, ZegoAudioRoute audioRoute) {
        imageView.setImageDrawable(ResourceUtils.getDrawable(R.drawable.selector_activity_speaker_voice_button));
        imageView.setSelected(false);
        imageView.setEnabled(true);
        switch (audioRoute) {
            case HEADPHONE:
                imageView.setEnabled(false);
                break;
            case BLUETOOTH:
                imageView.setEnabled(false);
                imageView.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_bluetooth));
                break;
            case SPEAKER:
                imageView.setSelected(true);
            default:
                break;
        }
    }
}