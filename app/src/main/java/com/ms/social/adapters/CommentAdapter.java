package com.ms.social.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.R;
import com.ms.social.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class CommentAdapter extends BaseAdapter {

    Context context;
    List<Comment> item;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference ref;
    FirebaseAuth fauth;

    public CommentAdapter(Context context, List<Comment> item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int position) {
        return item.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.comment_layout, null, false);
        ImageView image = convertView.findViewById(R.id.user_Image);
        TextView username = convertView.findViewById(R.id.user_name);
        TextView comment = convertView.findViewById(R.id.user_comment);
        db = FirebaseFirestore.getInstance();
        fauth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        ref = storage.getReference();
        Comment commentData = item.get(position);

        comment.setText(commentData.getMessageText());
        db.collection(COLLECTION_USERS).document(commentData.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                username.setText((String) documentSnapshot.get("username"));
            }
        });
        ref.child(COLLECTION_USERS)
                .child(commentData.getUserId())
                .child(USER_PROFILE_PICTURE).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).fit().centerInside().into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("tag", e.getMessage());
                image.setImageResource(R.drawable.ic_account_circle);
            }
        });


        return convertView;
    }
}
