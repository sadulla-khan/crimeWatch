package com.example.crime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ReportFragment;

import com.example.crime.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ActivityMainBinding binding ;
    //private boolean isTransitioning = false;
    private Fragment currentVisibleFragment = null;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            gotoActivity(LoginActivity.class);
        }
        /*else{
            boolean check = currentUser.isEmailVerified();
            if(!check) {
                gotoActivity(VarificationActivity.class);
            }

        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user= mAuth.getCurrentUser();
        BottomNavigationView bottomNavigationView= findViewById(R.id.bottomNavigationView);
        if(user != null) {
            String sanitizedEmail = sanitizeEmail(user.getEmail().toString().trim());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists, no need to add
                        Log.d(null,"User already exists: " + dataSnapshot.child("info").getValue());
                        replaceFragment(new HomeFragment(),"HomeFragment");
                        bottomNavigationView.setSelectedItemId(R.id.home);

                    } /*else {
                        gotoActivity(CollectInfoActivity.class);
                    }*/
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Database error: " + databaseError.getMessage());
                }
            });

        }



//bottom Navigation
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item ->{

            if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment(),"ProfileFragment");
            } else if (item.getItemId() == R.id.report) {
                replaceFragment(new ReportsFragment(),"ReportFragment");
            } else if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment(),"HomeFragment");
            } else if (item.getItemId() == R.id.map) {
                replaceFragment(new MapFragment(),"MapFragment");
            } else if (item.getItemId() == R.id.feed) {
                replaceFragment(new FeedFragment(),"FeedFragment");
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (currentVisibleFragment != null) {
            fragmentTransaction.hide(currentVisibleFragment);
        }

        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);

        if (targetFragment != null) {

            Log.d("FragmentManager", "Current Fragment: " + currentVisibleFragment.getTag());
            if (currentVisibleFragment != null && currentVisibleFragment.getTag().equals(tag)) {
                // Prevent duplicate transitions
                Log.d("FragmentTransaction", "Fragment " + tag + " is already visible.");
                fragmentTransaction.remove(currentVisibleFragment);
                fragmentTransaction.add(R.id.newFrame, fragment, tag);
                currentVisibleFragment = fragment;

            }else {
                fragmentTransaction.show(targetFragment);
                currentVisibleFragment = targetFragment;
            }
        } else {
            // If the target fragment doesn't exist, hide the current fragment and add the new one
            fragmentTransaction.add(R.id.newFrame, fragment, tag);
            currentVisibleFragment = fragment;
        }

        // Commit the transaction
        fragmentTransaction.commit();
        // new Handler(Looper.getMainLooper()).postDelayed(() -> isTransitioning = false, 300);
        logFragmentState();
    }


    public String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }
    private void logFragmentState() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            Log.d("FragmentState", "Fragment: " + fragment.getTag() + ", Visible: " + fragment.isVisible());
        }
    }


    private void gotoActivity(Class<?> clazz){
        Intent intent = new Intent(MainActivity.this, clazz);
        startActivity(intent);
        finish();

    }
}