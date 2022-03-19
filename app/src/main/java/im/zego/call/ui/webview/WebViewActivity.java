package im.zego.call.ui.webview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.blankj.utilcode.util.ActivityUtils;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import im.zego.call.databinding.ActivityWebViewBinding;
import im.zego.call.ui.BaseActivity;

public class WebViewActivity extends BaseActivity<ActivityWebViewBinding> {

    private static final String EXTRA_KEY_URL = "extra_key_url";
    private AgentWeb mAgentWeb;

    public static void startWebViewActivity(String url) {
        Activity topActivity = ActivityUtils.getTopActivity();
        Intent intent = new Intent(topActivity, WebViewActivity.class);
        intent.putExtra(EXTRA_KEY_URL, url);
        topActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(EXTRA_KEY_URL);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(binding.webViewContainer, new FrameLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    binding.webviewTitleText.setText(title);
                }
            })
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
            .createAgentWeb()
            .ready()
            .go(url);

        binding.webviewTitleBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mAgentWeb != null && !mAgentWeb.back()) {
            super.onBackPressed();
        }
    }
}