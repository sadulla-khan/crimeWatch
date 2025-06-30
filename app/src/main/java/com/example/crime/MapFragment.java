package com.example.crime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MapFragment extends Fragment {

    private WebView mapWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapWebView = view.findViewById(R.id.webViewMap);
        mapWebView.getSettings().setJavaScriptEnabled(true);
        mapWebView.getSettings().setAllowFileAccess(true);
        mapWebView.setWebViewClient(new WebViewClient());

        // Load the map HTML file from assets
        mapWebView.loadUrl("file:///android_asset/map_colored.html");

        mapWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // Simulated data, but this can come from Firebase
                String circleDataJson = "[{\"lat\":23.811,\"lon\":90.413,\"radius\":300,\"fillColor\":\"#f03\",\"borderColor\":\"red\",\"fillOpacity\":0.5,\"label\":\"High Crime Zone\"}," +
                        "{\"lat\":23.815,\"lon\":90.420,\"radius\":250,\"fillColor\":\"#ff9900\",\"borderColor\":\"orange\",\"fillOpacity\":0.5,\"label\":\"Medium Risk Zone\"}]";

                view.evaluateJavascript("addCirclesFromAndroid('" + circleDataJson + "')", null);
            }
        });
    }
}
