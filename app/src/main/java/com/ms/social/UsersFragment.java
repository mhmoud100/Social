package com.ms.social;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ms.social.model.User;

import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_USERS;

public class UsersFragment extends Fragment {
    List<String> item;
    FirebaseFirestore db;
    FirebaseAuth fauth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        ListView listView = view.findViewById(R.id.usersList);
        db = FirebaseFirestore.getInstance();
        fauth = FirebaseAuth.getInstance();
        db.collection(COLLECTION_USERS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        item = new ArrayList<>();
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                item.add(document.getId());
                            }
                            UserAdapter adapter = new UserAdapter(getContext(), item);
                            listView.setAdapter(adapter);
                        }else {
                            Log.i("tag", task.getException().toString());
                        }
                    }
                });
        return view;
    }
}