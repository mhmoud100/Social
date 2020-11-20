package com.ms.social.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.ms.social.R;
import com.ms.social.adapters.CommentAdapter;
import com.ms.social.model.Comment;
import com.ms.social.model.Post;

import static com.ms.social.help.Helper.*;

public class CommentActivity extends AppCompatActivity {
    ListView listView;
    Button comment;
    EditText commentText;
    FirebaseFirestore db;
    FirebaseAuth auth;
    CommentAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        listView  = findViewById(R.id.comments);
        comment = findViewById(R.id.comment);
        commentText = findViewById(R.id.comment_text);
        int position = getIntent().getIntExtra("position", 0);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db.collection(COLLECTION_POSTS).document(id.get(position)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.i("tag", "Failed", error);
                    return;
                }
                Post post = value.toObject(Post.class);
                adapter = new CommentAdapter(CommentActivity.this, post.getComments());
                listView.setAdapter(adapter);
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!commentText.getText().toString().trim().equals("")) {
                    Comment data = new Comment(auth.getCurrentUser().getUid(), commentText.getText().toString());
                    db.collection(COLLECTION_POSTS).document(id.get(position)).update("comments", FieldValue.arrayUnion(data));
                }
            }
        });


    }
}