package com.ms.social.fragments;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ms.social.R;
import com.ms.social.RecyclerViewAdapter;
import com.ms.social.model.Post;

import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_POSTS;

public class DiscoverFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    ArrayList<Post> posts;
    FirebaseFirestore db;
    public static ArrayList<String> id;
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getContext().getDrawable(R.drawable.divider_shape));
        recyclerView.addItemDecoration(itemDecor);
        display();
        return view;
    }
    private void display(){

        db.collection(COLLECTION_POSTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        posts = new ArrayList<>();
                        id = new ArrayList<>();
                        if (e != null) {
                            Log.i("tag", "Listen failed.", e);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {

                                Post post = doc.toObject(Post.class);
                                id.add(0, doc.getId());
                                posts.add(0, post);
                                adapter = new RecyclerViewAdapter(getContext(), posts);
                                recyclerView.setItemViewCacheSize(posts.size());
                                recyclerView.setAdapter(adapter);
                            }

                    }
                });

    }
}