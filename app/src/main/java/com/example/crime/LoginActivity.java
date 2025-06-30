package com.example.crime;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.lang.Class;

public class LoginActivity extends AppCompatActivity {

    private EditText userId, password, email;
    private Button loginButton;
    private TextView goToSignup;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        goToSignup = findViewById(R.id.goToSignup);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Simple validation and login logic
            if (mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter all fields", Toast.LENGTH_SHORT).show();
            } else {

                signIn(mail, pass);
            }
        });

        goToSignup.setOnClickListener(v -> {
            gotoActivity(SignupActivity.class);
        });


    }

    private void signIn(String mail, String pass){

        mAuth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successful.",
                                    Toast.LENGTH_SHORT).show();
                            gotoActivity(MainActivity.class);

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }


    private void gotoActivity(Class<?> clazz){
        Intent intent = new Intent(LoginActivity.this, clazz);
        startActivity(intent);
        finish();

    }
}