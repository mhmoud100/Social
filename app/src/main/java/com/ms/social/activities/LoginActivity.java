package com.ms.social.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ms.social.R;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout messageLayout, loadingLayout;
    private Button loginButton;
    private TextView createNewAccountTextView, forgotPasswordTextView;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        messageLayout = findViewById(R.id.layout_message);
        loadingLayout = findViewById(R.id.layout_loading);

        loginButton = findViewById(R.id.button_login);
        createNewAccountTextView = findViewById(R.id.text_view_create_new_account);
        forgotPasswordTextView = findViewById(R.id.text_view_forgot_password);

        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
    }

    private void enableLoginLayout() {
        emailEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        loginButton.setEnabled(true);
        createNewAccountTextView.setEnabled(true);
        forgotPasswordTextView.setEnabled(true);
    }

    private void disableLoginLayout() {
        emailEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        loginButton.setEnabled(false);
        createNewAccountTextView.setEnabled(false);
        forgotPasswordTextView.setEnabled(false);
    }

    private void showMessage(String message){
        messageLayout.setVisibility(View.VISIBLE);
        TextView messageTextView = findViewById(R.id.text_view_message);
        messageTextView.setText(message);
    }

    public void onClickLogin(View view) {

        loadingLayout.setVisibility(View.VISIBLE);
        disableLoginLayout();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()){
            loadingLayout.setVisibility(View.GONE);
            showMessage(getString(R.string.enter_correct_data));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loadingLayout.setVisibility(View.GONE);
            showMessage(getString(R.string.email_badly_formatted));
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, VerifyActivity.class));
                            }
                            LoginActivity.this.finishAffinity();
                        } else {
                            loadingLayout.setVisibility(View.GONE);
                            showMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    public void onClickCreateNewAccount(View view) {

        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));

    }

    public void onClickForgotPassword(View view) {

        disableLoginLayout();

        View resetPasswordLayout = LayoutInflater.from(this).inflate(R.layout.reset_password_layout, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(resetPasswordLayout);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enableLoginLayout();
            }
        });

        final EditText emailEditText = resetPasswordLayout.findViewById(R.id.edit_text_email);
        Button sendEmailButton = resetPasswordLayout.findViewById(R.id.button_send_email);

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    alertDialog.dismiss();
                    showMessage(getString(R.string.enter_correct_email));
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    alertDialog.dismiss();
                    showMessage(getString(R.string.email_badly_formatted));
                    return;
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        alertDialog.dismiss();
                        if (task.isSuccessful()) showMessage(getString(R.string.reset_password_email_sent));
                        else showMessage(task.getException().getMessage());
                    }
                });

            }
        });

    }

    public void onClickOk(View view) {
        messageLayout.setVisibility(View.GONE);
        enableLoginLayout();
    }

}