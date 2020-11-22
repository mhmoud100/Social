package com.ms.social.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
//import com.social18.R;
import com.google.firebase.storage.UploadTask;
import com.ms.social.R;
import com.ms.social.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class SignUpActivity extends AppCompatActivity {

    private LinearLayout messageLayout, loadingLayout;
    private CircleImageView profilePictureImageView;
    private EditText fullNameEditText, emailEditText, passwordEditText;
    private Spinner daysSpinner, monthsSpinner, yearsSpinner;
    private RadioButton maleRadioButton, femaleRadioButton;
    private Button createNewAccountButton;

    private Uri profilePicUri = null;
    private static final int CHOOSE_IMAGE_INTENT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        messageLayout = findViewById(R.id.layout_message);
        loadingLayout = findViewById(R.id.layout_loading);

        profilePictureImageView = findViewById(R.id.image_view_profile_picture);

        fullNameEditText = findViewById(R.id.edit_text_full_name);
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);

        daysSpinner = findViewById(R.id.spinner_days);
        monthsSpinner = findViewById(R.id.spinner_months);
        yearsSpinner = findViewById(R.id.spinner_years);

        maleRadioButton = findViewById(R.id.radio_button_male);
        femaleRadioButton = findViewById(R.id.radio_button_female);

        createNewAccountButton = findViewById(R.id.button_create_new_account);

    }

    private void enableSignUpLayout(){
        profilePictureImageView.setEnabled(true);
        fullNameEditText.setEnabled(true);
        emailEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        daysSpinner.setEnabled(true);
        monthsSpinner.setEnabled(true);
        yearsSpinner.setEnabled(true);
        maleRadioButton.setEnabled(true);
        femaleRadioButton.setEnabled(true);
        createNewAccountButton.setEnabled(true);
    }

    private void disableSignUpLayout(){
        profilePictureImageView.setEnabled(false);
        fullNameEditText.setEnabled(false);
        emailEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        daysSpinner.setEnabled(false);
        monthsSpinner.setEnabled(false);
        yearsSpinner.setEnabled(false);
        maleRadioButton.setEnabled(false);
        femaleRadioButton.setEnabled(false);
        createNewAccountButton.setEnabled(false);
    }

    private void showMessage(String message){
        messageLayout.setVisibility(View.VISIBLE);
        TextView messageTextView = findViewById(R.id.text_view_message);
        messageTextView.setText(message);
    }


    public void onClickProfilePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
        startActivityForResult(intent, CHOOSE_IMAGE_INTENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CHOOSE_IMAGE_INTENT_REQUEST_CODE && resultCode == RESULT_OK && intent != null){

            profilePicUri = intent.getData();
            profilePictureImageView.setImageURI(profilePicUri);

        }

    }

    public void onClickCreateNewAccount(View view) {

        loadingLayout.setVisibility(View.VISIBLE);
        disableSignUpLayout();

        final String fullName = fullNameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String day = daysSpinner.getSelectedItem().toString();
        final String month = monthsSpinner.getSelectedItem().toString();
        final String year = yearsSpinner.getSelectedItem().toString();

        String gender = null;
        if (maleRadioButton.isChecked()) gender = getString(R.string.male);
        else if (femaleRadioButton.isChecked()) gender = getString(R.string.female);

        final String finalGender = gender;

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || gender == null){
            loadingLayout.setVisibility(View.GONE);
            showMessage(getString(R.string.fields_are_empty));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loadingLayout.setVisibility(View.GONE);
            showMessage(getString(R.string.email_badly_formatted));
            return;
        }

        if (password.length() < 6){
            loadingLayout.setVisibility(View.GONE);
            showMessage(getString(R.string.password_must));
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    User user = new User(password, day, month, year, finalGender, "", fullName, new ArrayList<>(), new ArrayList<>());
                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(currentUser.getUid()).set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (profilePicUri != null) {
                                                        try {
                                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profilePicUri);
                                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                                                            byte[] bytes = byteArrayOutputStream.toByteArray();

                                                            FirebaseStorage.getInstance().getReference().child(COLLECTION_USERS)
                                                                    .child(currentUser.getUid())
                                                                    .child(USER_PROFILE_PICTURE).putBytes(bytes)
                                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    FirebaseStorage.getInstance().getReference().child(COLLECTION_USERS)
                                                                            .child(currentUser.getUid())
                                                                            .child(USER_PROFILE_PICTURE).getDownloadUrl()
                                                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                @Override
                                                                                public void onSuccess(Uri uri) {
                                                                                    profilePicUri = uri;
                                                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                            .setPhotoUri(profilePicUri)
                                                                                            .build();
                                                                                    FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();

                                                                                    userInfo.updateProfile(profileUpdates)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        Log.i("tag", "done");
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.i("tag", e.getMessage());
                                                                        }
                                                                    });

                                                                }
                                                            });



                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(fullName)
                                                            .build();
                                                    FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();

                                                    userInfo.updateProfile(profileUpdates)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.i("tag", "done");
                                                                    }
                                                                }
                                                            });
                                                    SignUpActivity.this.finishAffinity();
                                                    startActivity(new Intent(SignUpActivity.this, VerifyActivity.class));
                                                }
                                            });
                                }
                            });
                        } else {
                            loadingLayout.setVisibility(View.GONE);
                            showMessage(task.getException().getMessage());
                        }
                    }
                });

    }

    public void onClickOk(View view) {
        messageLayout.setVisibility(View.GONE);
        enableSignUpLayout();
    }

}