package com.ms.social.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ms.social.R;
import com.ms.social.help.Helper;
import com.ms.social.interfaces.ClickGotoProfileInterface;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.Result;

import static android.app.Activity.RESULT_OK;
import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.POST_PICTURE;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;


public class EditProfileFragment extends Fragment {

    ImageView profile_pic;
    EditText Name, Email, Password, Bio;
    Spinner Days, Months, Years;
    RadioButton Male, Female;
    Button Save;
    FirebaseFirestore db;
    StorageReference ref;
    Uri ProfilPicUri = null;
    FirebaseUser user;
    ClickGotoProfileInterface clickGotoProfileInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        clickGotoProfileInterface = (ClickGotoProfileInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profile_pic = view.findViewById(R.id.image_view_profile_picture);
        Name = view.findViewById(R.id.edit_text_full_name);
        Email = view.findViewById(R.id.edit_text_email);
        Password = view.findViewById(R.id.edit_text_password);
        Bio = view.findViewById(R.id.edit_text_Bio);
        Days = view.findViewById(R.id.spinner_days);
        Months = view.findViewById(R.id.spinner_months);
        Years = view.findViewById(R.id.spinner_years);
        Male = view.findViewById(R.id.radio_button_male);
        Female = view.findViewById(R.id.radio_button_female);
        Save = view.findViewById(R.id.button_save);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseStorage.getInstance().getReference();
        if (user.getPhotoUrl() != null){
            Picasso.with(getContext()).load(user.getPhotoUrl()).fit().centerCrop().into(profile_pic);
        } else {
            profile_pic.setImageResource(R.drawable.ic_account_circle);
        }
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
                startActivityForResult(intent, 1);

            }
        });
        Name.setText(user.getDisplayName());
        Email.setText(user.getEmail());
        String arr[] = getResources().getStringArray(R.array.months);
        db.collection(COLLECTION_USERS).document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot res = task.getResult();
                        Password.setText((String)res.get("password"));
                        Bio.setText((String)res.get("bio"));
                        Days.setSelection(Integer.parseInt((String)res.get("dayOfBirth"))-1);
                        String arr[] = getResources().getStringArray(R.array.months);
                        String arr1[] = getResources().getStringArray(R.array.years);
                        Months.setSelection(indexOf(arr, (String)res.get("monthOfBirth")));
                        Years.setSelection(indexOf(arr1, (String)res.get("yearOfBirth")));
                        if("Male".equals((String) res.get("gender"))){
                            Male.setChecked(true);
                        }else{
                            Female.setChecked(true);
                        }
                    }
                });
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProfilPicUri != null){
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), ProfilPicUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    ref.child(COLLECTION_USERS).child(user.getUid()).child(USER_PROFILE_PICTURE).putBytes(bytes)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.child(COLLECTION_USERS).child(user.getUid()).child(USER_PROFILE_PICTURE).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    ProfilPicUri = uri;
                                    UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(ProfilPicUri).build();
                                    user.updateProfile(req);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }

                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                        .setDisplayName(Name.getText().toString().trim()).build();
                user.updateProfile(req);
                db.collection(COLLECTION_USERS).document(user.getUid()).update(
                                        "bio",Bio.getText().toString().trim(),
                        "dayOfBirth",Days.getSelectedItem().toString(),
                                            "monthOfBirth",Months.getSelectedItem().toString(),
                                            "yearOfBirth",Years.getSelectedItem().toString(),
                                            "password",Password.getText().toString(),
                                            "username",Name.getText().toString());
                if(!(Email.getText().toString().trim()).equals(user.getEmail()) && !Patterns.EMAIL_ADDRESS.matcher(Email.getText().toString().trim()).matches()) {
                    user.updateEmail(Email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.sendEmailVerification();
                        }
                    });

                }
                user.updatePassword(Password.getText().toString());
                clickGotoProfileInterface.onClickGotoProfile();
            }
        });
        return view;
    }

    public int indexOf(String arr[],String s){
        int result = 0;
        for(int i = 0;i<arr.length;i++){
            if(s.equals(arr[i])) {
                result = i;
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            ProfilPicUri = data.getData();
            System.out.println("\n \n \n i am here \n \n \n");
            profile_pic.setImageURI(ProfilPicUri);
        }
    }
}