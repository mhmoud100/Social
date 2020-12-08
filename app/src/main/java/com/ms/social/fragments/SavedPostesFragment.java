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
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;
import com.ms.social.R;
import com.ms.social.adapters.PostAdapter;
import com.ms.social.model.Post;

import java.util.ArrayList;

import static com.ms.social.help.Helper.COLLECTION_POSTS;


public class SavedPostesFragment extends Fragment {
    RecyclerView recyclerView;
    PostAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth fauth;
    ArrayList<Post> posts;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved_postes, container, false);
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
        display();
        return view;
    }
    public void display(){

        db.collection(COLLECTION_POSTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    posts = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : task.getResult()){
                        Post post = documentSnapshot.toObject(Post.class);
                        if(post.getSaved_by().contains(fauth.getCurrentUser().getUid())){
                            post.setId(documentSnapshot.getId());
                            posts.add(post);
                        }
                    }
                    adapter = new PostAdapter(getContext(), posts);
                    recyclerView.setItemViewCacheSize(2);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
//
//