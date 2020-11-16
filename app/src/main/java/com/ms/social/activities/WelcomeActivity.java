package com.ms.social.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ms.social.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            if (currentUser.isEmailVerified()){
                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                WelcomeActivity.this.finishAffinity();
                return;
            }
            startActivity(new Intent(WelcomeActivity.this, VerifyActivity.class));
            WelcomeActivity.this.finishAffinity();
            return;
        }
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        WelcomeActivity.this.finishAffinity();
    }
}