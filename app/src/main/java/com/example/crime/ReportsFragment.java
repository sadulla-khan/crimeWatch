package com.example.crime;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText locationInput, descriptionInput, crimeTypeInput;
    private Button uploadMediaButton, reportButton, goHomeButton;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    private ImageView imagePreview;
    private Uri selectedImageUri;

    private String selectedLat = "";
    private String selectedLon = "";

    FirebaseAuth mAuth;


    public ReportsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportsFragment newInstance(String param1, String param2) {
        ReportsFragment fragment = new ReportsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);
                    }
                });

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("address");
                        selectedLat = result.getData().getStringExtra("latitude");
                        selectedLon = result.getData().getStringExtra("longitude");
                        locationInput.setText(address);
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false);

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Now 'view' is available for use
        crimeTypeInput = view.findViewById(R.id.editText_crimeType);
        locationInput = view.findViewById(R.id.editText_location);
        descriptionInput = view.findViewById(R.id.editText_description);

        uploadMediaButton = view.findViewById(R.id.button_upload_media);
        reportButton = view.findViewById(R.id.button_submit_report);
        //goHomeButton = view.findViewById(R.id.button_go_home);

        imagePreview = view.findViewById(R.id.image_preview);

        // Set up button listeners
        reportButton.setOnClickListener(v -> {
            String crime = crimeTypeInput.getText().toString();
            String location = locationInput.getText().toString();
            String description = descriptionInput.getText().toString();
            uploadToImgurAndSaveData(crime, location, description);
        });

        uploadMediaButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });





        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);
                    }
                });

        Button pickLocationBtn = view.findViewById(R.id.button_pick_location);
        pickLocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });



    }

    private void uploadToImgurAndSaveData(String crime, String location, String description) {
        if (selectedImageUri == null) {
            Toast.makeText(getActivity(), "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", Base64.encodeToString(imageBytes, Base64.DEFAULT))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .post(requestBody)
                    .addHeader("Authorization", "Client-ID 96f3ddebb89d726")
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("IMGUR_UPLOAD", "Upload failed: " + e.getMessage());
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Imgur upload failed", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("IMGUR_UPLOAD", "Response: " + responseBody);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getJSONObject("data").getString("link");

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }

                            String sanitizedEmail = sanitizeEmail(user.getEmail().trim());
                            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(sanitizedEmail)
                                    .child("reports");

                            String reportId = dbRef.push().getKey();
                            HashMap<String, String> report = new HashMap<>();
                            report.put("crime", crime);
                            report.put("location", location);
                            report.put("description", description);
                            report.put("imageUrl", imageUrl);
                            report.put("latitude",selectedLat);
                            report.put("longtitude",selectedLon);

                            dbRef.child(reportId).setValue(report).addOnCompleteListener(task -> {
                                requireActivity().runOnUiThread(() -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Firebase save failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("IMGUR_UPLOAD", "JSON parse error: " + e.getMessage());
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getActivity(), "JSON Parsing failed", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        Log.e("IMGUR_UPLOAD", "Upload failed with HTTP code: " + response.code());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Imgur upload failed: " + response.code(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    public String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }

}

