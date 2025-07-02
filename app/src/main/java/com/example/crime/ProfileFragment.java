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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button logout;

    private EditText name;
    private TextView email, addres;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private ImageView profileImage;
    private Button changeProfileImage;
    private Button changeName;
    private Button changeAddress;
    private Uri selectedProfileImageUri;
    private String selectedLat = "";
    private String selectedLon = "";
    private TextView locationInput;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private ActivityResultLauncher<Intent> mapPickerLauncher;
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
                        selectedProfileImageUri = result.getData().getData();
                        profileImage.setImageURI(selectedProfileImageUri);
                        uploadProfileImageToImgur();
                    }
                });
        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("address");
                        selectedLat = result.getData().getStringExtra("latitude");
                        selectedLon = result.getData().getStringExtra("longitude");
                        addres.setText(address);
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        String sanitizedEmail = sanitizeEmail(user.getEmail().trim());
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail).child("info");
                        dbRef.child("location").setValue(address);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       logout = view.findViewById(R.id.logout);
       name = view.findViewById(R.id.profileName);
       addres = view.findViewById(R.id.profileAddress);
       email = view.findViewById(R.id.profileEmail);
       FirebaseAuth mAuth = FirebaseAuth.getInstance();
       FirebaseUser user = mAuth.getCurrentUser();

       email.setText(user.getEmail());
        String sanitizedEmail = sanitizeEmail(user.getEmail().trim());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail).child("info");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("name").getValue() != null) {
                        name.setText(snapshot.child("name").getValue().toString().trim());
                    } else {
                        name.setText(""); // or set a placeholder
                    }

                    if (snapshot.child("location").getValue() != null) {
                        addres.setText(snapshot.child("location").getValue().toString().trim());
                    } else {
                        addres.setText("No address found");
                    }
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireActivity()).load(imageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       logout.setOnClickListener(v->{

           FirebaseAuth.getInstance().signOut();
           Intent intent = new Intent(requireContext(), MainActivity.class);
           startActivity(intent);
           requireActivity().finish();
       });
        profileImage = view.findViewById(R.id.profileImage);
        changeProfileImage = view.findViewById(R.id.changeProfileImage);

        changeProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        changeName = view.findViewById(R.id.changeName);

        changeName.setOnClickListener(v -> {
            String nam= name.getText().toString().trim();
            if (nam != null && !nam.isEmpty()) {
                dbRef.child("name").setValue(nam);
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }


        });
        changeAddress = view.findViewById(R.id.changeAddress);

        changeAddress.setOnClickListener(v -> {

                Intent intent = new Intent(getActivity(), MapPickerActivity.class);
                mapPickerLauncher.launch(intent);

        });

    }
    public String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }

    private void uploadProfileImageToImgur() {
        if (selectedProfileImageUri == null) return;

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedProfileImageUri);
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

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getJSONObject("data").getString("link");

                            // Save URL in Firebase
                            String sanitizedEmail = sanitizeEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(sanitizedEmail)
                                    .child("info");

                            dbRef.child("profileImage").setValue(imageUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}