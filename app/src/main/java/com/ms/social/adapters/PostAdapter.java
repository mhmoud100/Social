package com.ms.social.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.R;
import com.ms.social.activities.CommentActivity;

import com.ms.social.model.Post;
import com.ms.social.model.User;
import com.squareup.picasso.Picasso;




import java.util.ArrayList;


import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.POST_PICTURE;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> postitem;
    private Context context;
    FirebaseUser user;
    User ThatUser;
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

        if (item.getUserId().equals(user.getUid())) {
            holder.follow.setVisibility(View.GONE);
        }

        db.collection(COLLECTION_USERS).document(item.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                holder.userName.setText((String) task.getResult().get("username"));
            }
        });

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
                        .child(item.getId())
                        .child(POST_PICTURE).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                holder.progressBar.setVisibility(View.GONE);
                                Picasso.with(context).load(uri).fit().centerInside().into(holder.postImage);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.postImage.setVisibility(View.GONE);
                    }
                });



            db.collection(COLLECTION_USERS).document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    ThatUser = documentSnapshot.toObject(User.class);

                    if (ThatUser.getFollowing().contains(item.getUserId())){
                        holder.follow.setText("Following");
                        holder.follow.setTextColor(ContextCompat.getColor(context, R.color.black));
                        holder.follow.setBackgroundResource(R.drawable.off_white_selector);
                    }else {
                        holder.follow.setText("Follow");
                        holder.follow.setTextColor(ContextCompat.getColor(context, R.color.white));
                        holder.follow.setBackgroundResource(R.drawable.green_selector);
                    }
                }
            });


            holder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ThatUser.getFollowing().contains(item.getUserId())){
                        db.collection(COLLECTION_USERS).document(user.getUid()).update("following", FieldValue.arrayRemove(item.getUserId()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection(COLLECTION_USERS).document(item.getUserId()).update("followers", FieldValue.arrayRemove(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        notifyDataSetChanged();

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
                        db.collection(COLLECTION_USERS).document(user.getUid()).update("following", FieldValue.arrayUnion(item.getUserId()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection(COLLECTION_USERS).document(item.getUserId()).update("followers", FieldValue.arrayUnion(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        notifyDataSetChanged();

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


                    }
                }
            });

            if (item.getSaved_by().contains(user.getUid())){
                holder.save_post.setImageResource(R.drawable.ic_saved);
            }



                        if (item.getLiked_by().contains(user.getUid())){
                            holder.favoriteImage.setImageResource(R.drawable.ic_liked);
                            holder.favoriteText.setText(String.valueOf(item.getLiked_by().size()));
                            holder.favoriteText.setTextColor(ContextCompat.getColor(context, R.color.green));
                        } else {
                            holder.favoriteText.setText(String.valueOf(item.getLiked_by().size()));
                        }



                        if (item.getComments().size() != 0) {
                            for (int i = 0; i < item.getComments().size(); i++) {
                                if (item.getComments().get(i).getUserId().equals(user.getUid())){
                                    holder.commentImage.setImageResource(R.drawable.ic_commented);

                                    holder.commentText.setTextColor(ContextCompat.getColor(context, R.color.green));
                                }
                            }
                        }
        holder.commentText.setText(String.valueOf(item.getComments().size()));





            holder.favoriteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    db.collection(COLLECTION_POSTS).document(item.getId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Post post = documentSnapshot.toObject(Post.class);
                                    if (post.getLiked_by().contains(user.getUid())){
                                        db.collection(COLLECTION_POSTS).document(item.getId())
                                                .update("liked_by", FieldValue.arrayRemove(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                holder.favoriteImage.setImageResource(R.drawable.ic_favorite);
                                                holder.favoriteText.setText(String.valueOf(post.getLiked_by().size()));
                                                holder.favoriteText.setTextColor(ContextCompat.getColor(context, R.color.black));
                                                item.getLiked_by().remove(user.getUid());
                                                notifyDataSetChanged();
                                            }
                                        });
                                    } else {
                                        db.collection(COLLECTION_POSTS).document(item.getId())
                                                .update("liked_by", FieldValue.arrayUnion(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        item.getLiked_by().add(user.getUid());
                                                        notifyDataSetChanged();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                                    }
                                }
                            });

                }
            });

            holder.save_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection(COLLECTION_POSTS).document(item.getId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Post post = documentSnapshot.toObject(Post.class);
                                    if (post.getSaved_by().contains(user.getUid())){
                                        db.collection(COLLECTION_POSTS).document(item.getId())
                                                .update("saved_by", FieldValue.arrayRemove(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        holder.save_post.setImageResource(R.drawable.ic_bookmark);
                                                        item.getSaved_by().remove(user.getUid());
                                                        notifyDataSetChanged();

                                                    }
                                                });
                                    } else {
                                        db.collection(COLLECTION_POSTS).document(item.getId())
                                                .update("saved_by", FieldValue.arrayUnion(user.getUid()))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        item.getSaved_by().add(user.getUid());
                                                        notifyDataSetChanged();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                                    }
                                }
                            });
                }
            });
        holder.commentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("id", item.getId());
                context.startActivity(intent);
            }
        });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profileImage, postImage, save_post, favoriteImage, commentImage;
        TextView userName, date, postText, follow, favoriteText, commentText;
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
            save_post = view.findViewById(R.id.save_post_image);
            favoriteImage = view.findViewById(R.id.favorite_image);
            favoriteText = view.findViewById(R.id.favorite_text);
            commentImage = view.findViewById(R.id.comment_image);
            commentText = view.findViewById(R.id.comment_text);

        }

    }
}

