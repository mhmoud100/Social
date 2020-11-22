package com.ms.social.adapters;

import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.R;
import com.ms.social.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class UserAdapter extends BaseAdapter {
    Context context;
    List<String> item;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference ref;
    FirebaseAuth fauth;
    User ThatUser;

    public UserAdapter(Context context, List<String> item) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.user_layout, null, false);
        ImageView photo = convertView.findViewById(R.id.follow_Image);
        TextView name = convertView.findViewById(R.id.follow_user_name);
        TextView bio = convertView.findViewById(R.id.follow_bio);
        TextView follow = convertView.findViewById(R.id.follow);

        String uid = item.get(position);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        ref = storage.getReference();
        fauth = FirebaseAuth.getInstance();
        if (fauth.getCurrentUser().getUid().equals(uid)){
            follow.setVisibility(View.GONE);
        }
        db.collection(COLLECTION_USERS).document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name.setText((String) documentSnapshot.get("username"));
                bio.setText((String) documentSnapshot.get("bio"));
            }
        });
        db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.i("tag",error.getMessage());
                    return;
                }
                DocumentSnapshot documentSnapshot = value;
                ThatUser = documentSnapshot.toObject(User.class);
                List<String> list = ThatUser.getFollowing();
//                System.out.println(list);
                follow.setText("Follow");
                follow.setTextColor(ContextCompat.getColor(context, R.color.white));
                follow.setBackgroundResource(R.drawable.green_selector);
                if (list.isEmpty()){
                    follow.setText("Follow");
                } else {
//                    System.out.println(list.get(0));
                        if (list.contains(item.get(position))){
                            follow.setText("Following");
                            follow.setTextColor(ContextCompat.getColor(context, R.color.black));
                            follow.setBackgroundResource(R.drawable.off_white_selector);
                        }

                }
            }
        });
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ThatUser.getFollowing().contains(uid)){
                    db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).update("following", FieldValue.arrayRemove(uid))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    db.collection(COLLECTION_USERS).document(uid).update("followers", FieldValue.arrayRemove(fauth.getCurrentUser().getUid()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

//                                                    notifyDataSetChanged();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
//
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }else {
                    db.collection(COLLECTION_USERS).document(fauth.getCurrentUser().getUid()).update("following", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    db.collection(COLLECTION_USERS).document(uid).update("followers", FieldValue.arrayUnion(fauth.getCurrentUser().getUid()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
//                                                    follow.setText("Following");
//                                                    notifyDataSetChanged();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
//                                    follow.setText("Following");
//                                    notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                }
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
                Log.i("tag", e.getMessage());
                photo.setImageResource(R.drawable.ic_account_circle);
            }
        });
        return convertView;
    }
}
