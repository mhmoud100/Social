package com.ms.social.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ms.social.R;
import com.ms.social.interfaces.ClickAddPostInterface;
import com.ms.social.model.Post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.ms.social.help.Helper.COLLECTION_POSTS;
import static com.ms.social.help.Helper.POST_PICTURE;
import static java.lang.Thread.sleep;

public class CreatePostFragment extends Fragment {
    ImageView PostImage;
    TextView Gallery, Camera, Share;
    EditText AddPost;
    FirebaseAuth fauth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    String ImageId = "";
    Uri profilePicUri = null;
    private static final int CHOOSE_IMAGE_INTENT_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int ACTION_CAPTURE_CAMERA = 100;
    ClickAddPostInterface clickAddPostInterface;
    Bitmap imageBitmap;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        clickAddPostInterface = (ClickAddPostInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        PostImage = view.findViewById(R.id.post_image);
        Gallery = view.findViewById(R.id.Gallary);
        Camera = view.findViewById(R.id.Camera);
        Share = view.findViewById(R.id.share_post);
        AddPost = view.findViewById(R.id.add_post);
        fauth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
                startActivityForResult(intent, CHOOSE_IMAGE_INTENT_REQUEST_CODE);
            }
        });

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, ACTION_CAPTURE_CAMERA);
                }else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        Share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("KK:mm a dd MMM yyyy", Locale.getDefault());
                String currentDateAndTime = df.format(new Date());

                Post post = new Post(fauth.getCurrentUser().getUid(),
                        AddPost.getText().toString(),
                        currentDateAndTime,new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                if (profilePicUri != null) {
                    db.collection(COLLECTION_POSTS).add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            ImageId = documentReference.getId();

                            final ProgressDialog progressDialog = new ProgressDialog(getContext());
                            progressDialog.setTitle("Uploading...");
                            progressDialog.show();
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), profilePicUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                            byte[] bytes = byteArrayOutputStream.toByteArray();
                            storageRef.child(COLLECTION_POSTS)
                                    .child(fauth.getCurrentUser().getUid())
                                    .child(ImageId)
                                    .child(POST_PICTURE).putBytes(bytes)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            progressDialog.dismiss();
                                            clickAddPostInterface.onClickAddPost();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                } else if (imageBitmap != null){
                    db.collection(COLLECTION_POSTS).add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            ImageId = documentReference.getId();

                            final ProgressDialog progressDialog = new ProgressDialog(getContext());
                            progressDialog.setTitle("Uploading...");
                            progressDialog.show();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                            byte[] bytes = byteArrayOutputStream.toByteArray();
                            storageRef.child(COLLECTION_POSTS)
                                    .child(fauth.getCurrentUser().getUid())
                                    .child(ImageId)
                                    .child(POST_PICTURE).putBytes(bytes)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            progressDialog.dismiss();
                                            clickAddPostInterface.onClickAddPost();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                } else {
                        db.collection(COLLECTION_POSTS).add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                clickAddPostInterface.onClickAddPost();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                }

            }
        });



        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_CAPTURE_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
            else
            {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CHOOSE_IMAGE_INTENT_REQUEST_CODE && resultCode == RESULT_OK && intent != null){

            profilePicUri = intent.getData();
            PostImage.setVisibility(View.VISIBLE);
            PostImage.setImageURI(profilePicUri);
            System.out.println(profilePicUri);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && intent != null) {
            Bundle extras = intent.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            PostImage.setVisibility(View.VISIBLE);
            PostImage.setImageBitmap(imageBitmap);
        }

    }
}