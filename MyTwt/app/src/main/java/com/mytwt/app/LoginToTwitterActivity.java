package com.mytwt.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginToTwitterActivity extends Activity {
    protected static final String CALLBACK_URL_KEY = "CALLBACK_URL_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_to_twitter);

        Intent intent = getIntent();
        String mUrl = intent.getStringExtra(MainActivity.AUTHENTICATION_URL_KEY);

        WebView webView = (WebView) findViewById(R.id.logintotwitter_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new LoginToTwitterWebViewClient());
        Log.d("TestURL", "begin oauth");
        webView.loadUrl(mUrl);
        Log.d("TestURL", "after oauth");
    }

    private class LoginToTwitterWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("TestURL", "entered shouldover");
            Log.d("TestURL", url);
            if (url.startsWith(getString(R.string.TWITTER_CALLBACK_URL))) {
                Log.d("TestURL", "callback url recieved");
                Log.d("TestURL", url);
                Intent intent = new Intent();
                intent.putExtra(CALLBACK_URL_KEY, url);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            return false;
        }
    }
}
