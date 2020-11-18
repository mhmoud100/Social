package com.ms.social.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ms.social.UserAdapter;
import com.ms.social.R;
import com.ms.social.model.User;

import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_USERS;

public class FollowingFragment extends Fragment {
    List<String> item;
    FirebaseFirestore db;
    FirebaseAuth fauth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        ListView listView = view.findViewById(R.id.followingList);
        db = FirebaseFirestore.getInstance();
        fauth = FirebaseAuth.getInstance();
        db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                item = new ArrayList<>();
                User Follow = documentSnapshot.toObject(User.class);
                item = Follow.getFollowing();
                UserAdapter adapter = new UserAdapter(getContext(), item);
                listView.setAdapter(adapter);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        return view;
    }
}