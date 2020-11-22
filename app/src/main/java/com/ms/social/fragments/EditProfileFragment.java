package com.ms.social.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.R;
import com.ms.social.help.Helper;
import com.squareup.picasso.Picasso;

import static com.ms.social.help.Helper.COLLECTION_USERS;


public class EditProfileFragment extends Fragment {

    ImageView profile_pic;
    EditText Name, Email, Password, Bio;
    Spinner Days, Months, Years;
    RadioButton Male, Female;
    Button Save;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference ref;
    FirebaseUser user;
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
        Picasso.with(getContext()).load(user.getPhotoUrl()).fit().centerCrop().into(profile_pic);
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
                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                        .setDisplayName(Name.getText().toString().trim()).build();
                user.updateProfile(req);
                db.collection(COLLECTION_USERS).document(user.getUid()).update("bio",Bio.getText().toString().trim(),
                        "dayOfBirth",Days.getSelectedItem().toString(),
                                            "monthOfBirth",Months.getSelectedItem().toString(),
                                            "yearOfBirth",Years.getSelectedItem().toString(),
                                            "password",Password.getText().toString(),
                                            "username",Name.getText().toString());
                if(!(Email.getText().toString().trim()).equals(user.getEmail())) {
                    user.updateEmail(Email.getText().toString().trim());
                    user.sendEmailVerification();
                }
                user.updatePassword(Password.getText().toString());
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
}