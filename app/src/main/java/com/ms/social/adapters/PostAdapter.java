package com.ms.social.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.R;
import com.ms.social.help.Helper;
import com.ms.social.model.Post;
import com.ms.social.model.User;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.POST_PICTURE;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;
import static java.lang.Thread.sleep;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> postitem;
    private Context context;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference reference;
    Boolean b = true;

    public PostAdapter(Context context, ArrayList<Post> postitem) {
        this.context = context;
        this.postitem = postitem;
    }
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public int getItemCount(){
        return postitem.size();
    }

    public void onBindViewHolder (ViewHolder holder, final int position) {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        holder.progressBar.setVisibility(View.VISIBLE);
        final Post item = postitem.get(position);
        holder.postText.setText(item.getText());
        holder.date.setText(item.getDate());
        holder.userName.setText(item.getUserName());
        if (item.getUserId().equals(user.getUid())) {
            holder.follow.setVisibility(View.GONE);
        }
        reference.child(COLLECTION_USERS)
                .child(item.getUserId())
                .child(USER_PROFILE_PICTURE).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(context).load(uri).fit().centerInside().into(holder.profileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.profileImage.setImageResource(R.drawable.ic_account_circle);
                Log.i("tag", e.getMessage());
            }
        });

                reference.child(COLLECTION_POSTS)
                        .child(item.getUserId())
                        .child(Helper.id.get(position))
                        .child(POST_PICTURE).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                holder.progressBar.setVisibility(View.GONE);
                                Picasso.with(context).load(uri).fit().centerInside().into(holder.postImage);
//                            notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (holder.postImage.getDrawable() == null) {
                            if (b) {
                                try {
                                    sleep(5000);
                                    notifyDataSetChanged();
                                    b = false;
                                } catch (InterruptedException interruptedException) {
                                    interruptedException.printStackTrace();
                                }

                            } else {
                                holder.progressBar.setVisibility(View.GONE);
                                holder.postImage.setVisibility(View.GONE);
                            }
                        }
                    }
                });



            db.collection(COLLECTION_USERS).document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null){
                        Log.i("tag",error.getMessage());
                        return;
                    }
                    DocumentSnapshot documentSnapshot = value;
                    User Follow = documentSnapshot.toObject(User.class);
                    List<String> list = Follow.getFollowing();
                    System.out.println(list);
                    if (list.isEmpty()){
                        holder.follow.setText("Follow");
                    } else {
                        System.out.println(list.get(0));
                        for (int i = 0; i < list.size(); i++){
                            if (list.get(i).equals(item.getUserId())){
                                holder.follow.setText("Following");
                                holder.follow.setTextColor(ContextCompat.getColor(context, R.color.black));
                                holder.follow.setBackgroundResource(R.drawable.off_white_selector);
                            }
                        }
                    }
                }
            });

            holder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection(COLLECTION_USERS).document(user.getUid()).update("following", FieldValue.arrayUnion(item.getUserId()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    db.collection(COLLECTION_USERS).document(item.getUserId()).update("followers", FieldValue.arrayUnion(user.getUid()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    holder.follow.setText("Following");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                    holder.follow.setText("Following");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });



                }
            });

    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profileImage, postImage;
        TextView userName, date, postText, follow;
        ProgressBar progressBar;
        ViewHolder(View view){
            super(view);
            profileImage = view.findViewById(R.id.user_profile_picture);
            postImage = view.findViewById(R.id.post_image);
            userName = view.findViewById(R.id.post_user_name);
            date = view.findViewById(R.id.post_date);
            postText = view.findViewById(R.id.post_title);
            follow = view.findViewById(R.id.follow);
            progressBar = view.findViewById(R.id.progress);

        }

    }
}

