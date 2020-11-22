package com.ms.social.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ms.social.adapters.PostAdapter;
import com.ms.social.R;
import com.ms.social.help.Helper;
import com.ms.social.interfaces.ClickGotoEditInterface;
import com.ms.social.interfaces.ClickGotoFollowersInterface;
import com.ms.social.interfaces.ClickGotoFollowingInterface;
import com.ms.social.model.Post;
import com.ms.social.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;
import static com.ms.social.help.Helper.id;

public class ProfileFragment extends Fragment {
ImageView profileImage;
TextView profileName, profileBio, followersNumber, followingNumber;
Button EditProfile, Followers, Following;
RecyclerView recyclerView;
FirebaseAuth fauth;
FirebaseFirestore db;

ArrayList<Post> posts;
PostAdapter adapter;
ClickGotoEditInterface clickGotoEditInterface;
ClickGotoFollowersInterface clickGotoFollowersInterface;
ClickGotoFollowingInterface clickGotoFollowingInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        clickGotoEditInterface = (ClickGotoEditInterface) context;
        clickGotoFollowersInterface = (ClickGotoFollowersInterface) context;
        clickGotoFollowingInterface = (ClickGotoFollowingInterface) context;
    }

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
        EditProfile = view.findViewById(R.id.edit);
        Followers = view.findViewById(R.id.followers);
        Following = view.findViewById(R.id.following);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getContext().getDrawable(R.drawable.divider_shape));
        recyclerView.addItemDecoration(itemDecor);
        fauth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        display();


        db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    profileName.setText(fauth.getCurrentUser().getDisplayName());
                    profileBio.setText(user.getBio());
                    followersNumber.setText(String.valueOf(user.getFollowers().size()));
                    followingNumber.setText(String.valueOf(user.getFollowing().size()));


                }else {
                    Log.i("tag", "Failed",task.getException());
                }
            }
        });
        if (fauth.getCurrentUser().getPhotoUrl() != null) {
            Picasso.with(getContext()).load(fauth.getCurrentUser().getPhotoUrl()).fit().centerCrop().into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }


        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickGotoEditInterface.onClickGotoEdit();
            }
        });

        Following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickGotoFollowingInterface.onClickGotoFollowing();
            }
        });

        Followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickGotoFollowersInterface.onClickGotoFollowers();
            }
        });


        return view;
    }
    public void display(){
        db.collection(COLLECTION_POSTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                posts = new ArrayList<>();
                id = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult()){
                    Post post = documentSnapshot.toObject(Post.class);
                    if(post.getUserId().equals(fauth.getCurrentUser().getUid())){
                        id.add(0,documentSnapshot.getId());
                        posts.add(0,post);
                    }
                }
                adapter = new PostAdapter(getContext(), posts);
                recyclerView.setItemViewCacheSize(posts.size());
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
//
//