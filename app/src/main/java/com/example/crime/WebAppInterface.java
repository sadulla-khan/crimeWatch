package com.example.crime;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    Activity activity;

    WebAppInterface(Activity context) {
        this.activity = context;
    }

    @JavascriptInterface
    public void sendLocation(String lat, String lon, String address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", lat);
        resultIntent.putExtra("longitude", lon);
        resultIntent.putExtra("address", address);
        activity.setResult(Activity.RESULT_OK, resultIntent);
        activity.finish();
    }
}
