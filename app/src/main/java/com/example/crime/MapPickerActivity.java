package com.example.crime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MapPickerActivity extends Activity {
    WebView mapWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapWebView = new WebView(this);
        setContentView(mapWebView);

        mapWebView.getSettings().setJavaScriptEnabled(true);
        mapWebView.setWebChromeClient(new WebChromeClient());
        mapWebView.setWebViewClient(new WebViewClient());

        mapWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        mapWebView.loadUrl("file:///android_asset/map_picker.html");
    }
}

