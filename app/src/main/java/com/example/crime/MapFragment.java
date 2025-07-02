package com.example.crime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Map;

public class MapFragment extends Fragment {

    private WebView mapWebView;
    EditText editCrimeType, editDate, editTime;
    Button btnPredict;
    private CrimePredictor predictor;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editCrimeType = view.findViewById(R.id.editCrimeType);
        editDate = view.findViewById(R.id.editDate);
        editTime = view.findViewById(R.id.editTime);
        btnPredict = view.findViewById(R.id.btnPredict);

        mapWebView = view.findViewById(R.id.webViewMap);
        mapWebView.getSettings().setJavaScriptEnabled(true);
        mapWebView.getSettings().setAllowFileAccess(true);
        mapWebView.setWebViewClient(new WebViewClient());

        Map<String, Integer> crimeTypeMap = Map.ofEntries(
                Map.entry("assault", 0),
                Map.entry("domestic violence", 1),
                Map.entry("drug trafficking", 2),
                Map.entry("human trafficking", 3),
                Map.entry("kidnapping", 4),
                Map.entry("murder", 5),
                Map.entry("rape", 6),
                Map.entry("robbery", 7),
                Map.entry("sexual harassment", 8),
                Map.entry("snatching", 9),
                Map.entry("theft", 10)
        );

        Map<String, Integer> placeMap = Map.ofEntries(
                Map.entry("Adabor", 0), Map.entry("Airport", 1), Map.entry("Badda", 2), Map.entry("Banani", 3),
                Map.entry("Bimanbandar", 4), Map.entry("Cantonment", 5), Map.entry("Darus Salam", 6), Map.entry("Demra", 7),
                Map.entry("Dhanmondi", 8), Map.entry("Gulshan", 9), Map.entry("Hazaribagh", 10), Map.entry("Jatrabari", 11),
                Map.entry("Kafrul", 12), Map.entry("Kalabagan", 13), Map.entry("Kamrangirchar", 14), Map.entry("Khilgaon", 15),
                Map.entry("Kotwali", 16), Map.entry("Lalbagh", 17), Map.entry("Mirpur", 18), Map.entry("Mohammadpur", 19),
                Map.entry("Motijheel", 20), Map.entry("New Market", 21), Map.entry("Pallabi", 22), Map.entry("Paltan", 23),
                Map.entry("Ramna", 24), Map.entry("Rampura", 25), Map.entry("Sabujbagh", 26), Map.entry("Shah Ali", 27),
                Map.entry("Shahbagh", 28), Map.entry("Shyampur", 29), Map.entry("Sutrapur", 30), Map.entry("Tejgaon", 31),
                Map.entry("Tejgaon Industrial Area", 32), Map.entry("Turag", 33), Map.entry("Uttara", 34), Map.entry("Wari", 35)
        );



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

        btnPredict.setOnClickListener(v -> {
            String crimeStr = editCrimeType.getText().toString().trim().toLowerCase();
            String dateStr = editDate.getText().toString().trim();
            String timeStr = editTime.getText().toString().trim();

            Integer crimeCode = crimeTypeMap.get(crimeStr);
            if (crimeCode == null) {
                Toast.makeText(getContext(), "Unknown crime type", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Map.Entry<String, Integer> entry : placeMap.entrySet()) {
                String placeName = entry.getKey();
                int placeCode = entry.getValue();

                float[] features = extractFeatures(placeCode, crimeCode, dateStr, timeStr);
                if (features == null) continue;

                float prediction = 0;
                try {
                    prediction = predictor.predictCrime(features);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // hardcode coords for now, ideally use a map of lat/lon for each place
                double lat = getLatForPlace(placeName);
                double lon = getLonForPlace(placeName);

                String color = prediction > 0.5 ? "#f03" : "#ffff00";
                String border = prediction > 0.5 ? "red" : "yellow";

                String json = "[{\"lat\":" + lat + ",\"lon\":" + lon + ",\"radius\":300," +
                        "\"fillColor\":\"" + color + "\",\"borderColor\":\"" + border + "\",\"fillOpacity\":0.5," +
                        "\"label\":\"" + placeName + ": " + (prediction > 0.5 ? "High Risk" : "Low Risk") + "\"}]";

                mapWebView.evaluateJavascript("addCirclesFromAndroid('" + json + "')", null);
            }
        });

    }

    private float[] extractFeatures(int placeEncoded, int crimeEncoded, String dateStr, String timeStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);

            int hour = time.getHour();
            int dayOfWeek = date.getDayOfWeek().getValue() % 7; // Sunday = 0
            int month = date.getMonthValue();
            int weekOfYear = date.get(WeekFields.ISO.weekOfWeekBasedYear());
            int isWeekend = (dayOfWeek == 0 || dayOfWeek == 6) ? 1 : 0;

            return new float[] {
                    placeEncoded,
                    crimeEncoded,
                    hour,
                    dayOfWeek,
                    month,
                    isWeekend,
                    weekOfYear
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private double getLatForPlace(String placeName) {
        switch (placeName.toLowerCase()) {
            case "adabor": return 23.7740;
            case "airport": return 23.8423;
            case "badda": return 23.7806;
            case "banani": return 23.7936;
            case "bimanbandar": return 23.8508;
            case "cantonment": return 23.8125;
            case "darus salam": return 23.8063;
            case "demra": return 23.7101;
            case "dhanmondi": return 23.7461;
            case "gulshan": return 23.7916;
            case "hazaribagh": return 23.7361;
            case "jatrabari": return 23.7103;
            case "kafrul": return 23.7922;
            case "kalabagan": return 23.7432;
            case "kamrangirchar": return 23.7182;
            case "khilgaon": return 23.7471;
            case "kotwali": return 23.7087;
            case "lalbagh": return 23.7186;
            case "mirpur": return 23.8041;
            case "mohammadpur": return 23.7608;
            case "motijheel": return 23.7339;
            case "new market": return 23.7275;
            case "pallabi": return 23.8286;
            case "paltan": return 23.7366;
            case "ramna": return 23.7414;
            case "rampura": return 23.7639;
            case "sabujbagh": return 23.7382;
            case "shah ali": return 23.8160;
            case "shahbagh": return 23.7362;
            case "shyampur": return 23.6945;
            case "sutrapur": return 23.7083;
            case "tejgaon": return 23.7578;
            case "tejgaon industrial area": return 23.7635;
            case "turag": return 23.8590;
            case "uttara": return 23.8747;
            case "wari": return 23.7096;
            default: return 23.8103; // fallback center
        }
    }

    private double getLonForPlace(String placeName) {
        switch (placeName.toLowerCase()) {
            case "adabor": return 90.3584;
            case "airport": return 90.3978;
            case "badda": return 90.4265;
            case "banani": return 90.4043;
            case "bimanbandar": return 90.4086;
            case "cantonment": return 90.3885;
            case "darus salam": return 90.3531;
            case "demra": return 90.4729;
            case "dhanmondi": return 90.3742;
            case "gulshan": return 90.4167;
            case "hazaribagh": return 90.3652;
            case "jatrabari": return 90.4355;
            case "kafrul": return 90.3785;
            case "kalabagan": return 90.3796;
            case "kamrangirchar": return 90.3441;
            case "khilgaon": return 90.4186;
            case "kotwali": return 90.4070;
            case "lalbagh": return 90.3860;
            case "mirpur": return 90.3667;
            case "mohammadpur": return 90.3585;
            case "motijheel": return 90.4120;
            case "new market": return 90.3864;
            case "pallabi": return 90.3663;
            case "paltan": return 90.4080;
            case "ramna": return 90.4012;
            case "rampura": return 90.4229;
            case "sabujbagh": return 90.4305;
            case "shah ali": return 90.3603;
            case "shahbagh": return 90.3950;
            case "shyampur": return 90.4589;
            case "sutrapur": return 90.4142;
            case "tejgaon": return 90.3946;
            case "tejgaon industrial area": return 90.4089;
            case "turag": return 90.3502;
            case "uttara": return 90.4000;
            case "wari": return 90.4216;
            default: return 90.4125; // fallback
        }
    }


}
