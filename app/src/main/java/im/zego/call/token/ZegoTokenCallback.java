package im.zego.call.token;

import androidx.annotation.Nullable;

/**
 * Created by rocket_wang on 2022/4/9.
 */
public interface ZegoTokenCallback {
    void onTokenCallback(int errorCode, @Nullable String token);
}
