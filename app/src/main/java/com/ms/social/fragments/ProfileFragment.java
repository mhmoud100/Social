package com.ms.social.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.PostAdapter;
import com.ms.social.R;
import com.ms.social.help.Helper;
import com.ms.social.model.Post;
import com.ms.social.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;
import static com.ms.social.help.Helper.id;

public class ProfileFragment extends Fragment {
ImageView profileImage;
TextView profileName, profileBio, followersNumber, followingNumber;
RecyclerView recyclerView;
FirebaseAuth fauth;
FirebaseFirestore db;
FirebaseStorage storage;
StorageReference ref;
ArrayList<Post> posts;
PostAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.user_image_profile);
        profileName = view.findViewById(R.id.user_name_profile);
        profileBio = view.findViewById(R.id.user_bio_profile);
        followersNumber = view.findViewById(R.id.followers_number);
        followingNumber = view.findViewById(R.id.following_number);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fauth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        ref = storage.getReference();
        display();
        db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    profileName.setText(user.getUsername());
                    profileBio.setText(user.getBio());
                    followersNumber.setText(String.valueOf(user.getFollowers().size()));
                    followingNumber.setText(String.valueOf(user.getFollowing().size()));


                }else {
                    Log.i("tag", "Failed",task.getException());
                }
            }
        });

        ref.child(COLLECTION_USERS)
                .child(fauth.getCurrentUser().getUid())
                .child(USER_PROFILE_PICTURE).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        return view;
    }
    public void display(){
        posts = new ArrayList<>();
        id = new ArrayList<>();
        db.collection(COLLECTION_POSTS).whereEqualTo("userId", fauth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null){
                    Log.i("tag", "Fail", error);
                }

                for (QueryDocumentSnapshot documentSnapshot : value){
                    id.add(0,documentSnapshot.getId());
                    Post post = documentSnapshot.toObject(Post.class);
                    posts.add(0,post);
                }
                adapter = new PostAdapter(getContext(), posts);
                recyclerView.setItemViewCacheSize(2);
                recyclerView.setAdapter(adapter);

            }
        });
    }
}