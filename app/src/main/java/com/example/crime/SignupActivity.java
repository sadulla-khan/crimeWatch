package com.example.crime;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private EditText email, password, confirmPassword,  name;
    private Button signupButton, chooseLocation;

    private TextView goToLogin, address;

    private ActivityResultLauncher<Intent> mapPickerLauncher;

    private String selectedLat = "";
    private String selectedLon = "";

    private FirebaseAuth mAuth;
    // ...
// Initialize Firebase Auth
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //if user email is varified
            boolean check = currentUser.isEmailVerified();
            if(check) {
                //Either of those case we let the main activity handle
                gotoActivity(MainActivity.class);
            }
        }

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String addresss = result.getData().getStringExtra("address");
                        selectedLat = result.getData().getStringExtra("latitude");
                        selectedLon = result.getData().getStringExtra("longitude");
                        address.setText(addresss);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //getting all the buttons and text field
        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        confirmPassword = findViewById(R.id.signupConfirmPassword);
        address = findViewById(R.id.signupAddress);
        signupButton = findViewById(R.id.signupButton);
        goToLogin = findViewById(R.id.goToLogin);
        chooseLocation = findViewById(R.id.signupChooseLocation);
        name = findViewById(R.id.signupName);

        mAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(v -> {
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();
            String nam = name.getText().toString().trim();
            String addres = address.getText().toString().trim();

            // Simple validation and signup logic
            if (mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || nam.isEmpty() || addres.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Enter all fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                signUp(mail,pass,nam, addres);

            }
        });

        goToLogin.setOnClickListener(v -> {
            gotoActivity(LoginActivity.class);
        });

        chooseLocation.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });
    }

    private void signUp(String mail,String pass, String nam, String addres){

        mAuth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //gotoActivity(VarificationActivity.class);

                            String sanitizedEmail = sanitizeEmail(user.getEmail().trim());

                            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(sanitizedEmail)
                                    .child("info");

                            HashMap<String, String> infos = new HashMap<>();
                            infos.put("name", nam);  // ✅ corrected key
                            infos.put("location", addres);
                            infos.put("latitude", String.valueOf(selectedLat));
                            infos.put("longitude", String.valueOf(selectedLon)); // ✅ corrected key

                            dbRef.setValue(infos).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "User info saved successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
                                }
                                gotoActivity(MainActivity.class); // ✅ move inside this block so it's called after save attempt
                            });
                            gotoActivity(MainActivity.class);



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    private void gotoActivity(Class<?> clazz){
        Intent intent = new Intent(SignupActivity.this, clazz);
        startActivity(intent);
        finish();

    }

    public String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }
}