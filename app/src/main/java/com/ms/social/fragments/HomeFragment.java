package com.ms.social.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.QuerySnapshot;
import com.ms.social.adapters.PostAdapter;
import com.ms.social.R;
import com.ms.social.model.Post;
import com.ms.social.model.User;

import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;


public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    PostAdapter adapter;
    ArrayList<Post> posts;
    FirebaseFirestore db;
    FirebaseAuth fauth;
    List<String> followinglist;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        db = FirebaseFirestore.getInstance();
        fauth = FirebaseAuth.getInstance();
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getContext().getDrawable(R.drawable.divider_shape));
        recyclerView.addItemDecoration(itemDecor);
        followinglist = new ArrayList<>();
        db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.i("tag",error.getMessage());
                    return;
                }
                DocumentSnapshot documentSnapshot = value;
                User Follow = documentSnapshot.toObject(User.class);
                followinglist = Follow.getFollowing();
            }
        });
        display();
        return view;
    }

    private void display() {

        db.collection(COLLECTION_POSTS)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    posts = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (followinglist.contains(doc.get("userId"))) {
                            Post post = doc.toObject(Post.class);
                            post.setId(doc.getId());
                            posts.add(post);
                        } else if (doc.get("userId").equals(fauth.getCurrentUser().getUid())) {
                            Post post = doc.toObject(Post.class);
                            post.setId(doc.getId());
                            posts.add(post);

                        }
                    }
                    adapter = new PostAdapter(getContext(), posts);
                    recyclerView.setItemViewCacheSize(posts.size());
                    recyclerView.setAdapter(adapter);
                }else {
                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
