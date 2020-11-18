package com.ms.social;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class ListViewAdapter extends BaseAdapter {
    Context context;
    List<String> item;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference ref;

    public ListViewAdapter(Context context, List<String> item) {
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

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.follow_layout, null, false);
        ImageView photo = convertView.findViewById(R.id.follow_Image);
        TextView name = convertView.findViewById(R.id.follow_user_name);
        TextView bio = convertView.findViewById(R.id.follow_bio);
        String uid = item.get(position);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        ref = storage.getReference();
        db.collection(COLLECTION_USERS).document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name.setText((String) documentSnapshot.get("username"));
                bio.setText((String) documentSnapshot.get("bio"));
            }
        });
        ref.child(COLLECTION_USERS)
                .child(uid)
                .child(USER_PROFILE_PICTURE).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).fit().centerInside().into(photo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                photo.setImageResource(R.drawable.ic_account_circle);
            }
        });
        return convertView;
    }
}
