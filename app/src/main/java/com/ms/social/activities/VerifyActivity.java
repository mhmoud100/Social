package com.ms.social.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ms.social.R;

import java.util.Objects;

import static com.ms.social.help.Helper.*;

public class VerifyActivity extends AppCompatActivity {

    private LinearLayout messageLayout, loadingLayout;
    private Button startUsingSocialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        messageLayout = findViewById(R.id.layout_message);
        loadingLayout = findViewById(R.id.layout_loading);
        startUsingSocialButton = findViewById(R.id.button_start_using_social);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.verify_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
                VerifyActivity.this.finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String message){
        messageLayout.setVisibility(View.VISIBLE);
        TextView messageTextView = findViewById(R.id.text_view_message);
        messageTextView.setText(message);
    }

    public void onClickStartUsingSocial(View view) {
        loadingLayout.setVisibility(View.VISIBLE);
        startUsingSocialButton.setEnabled(false);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
                .document(Objects.requireNonNull(currentUser).getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    String email = currentUser.getEmail();
                    String password = String.valueOf(task.getResult().get("password"));
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(Objects.requireNonNull(email), password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified()) {
                                            VerifyActivity.this.finishAffinity();
                                            startActivity(new Intent(VerifyActivity.this, HomeActivity.class));
                                        } else {
                                            loadingLayout.setVisibility(View.GONE);
                                            showMessage(getString(R.string.check_your_email));
                                        }

                                    } else {
                                        loadingLayout.setVisibility(View.GONE);
                                        showMessage(Objects.requireNonNull(task.getException()).getMessage());
                                    }
                                }
                            });
                } else {
                    loadingLayout.setVisibility(View.GONE);
                    showMessage(Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }

    public void onClickOk(View view) {
        messageLayout.setVisibility(View.GONE);
        startUsingSocialButton.setEnabled(true);
    }
}