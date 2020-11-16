package com.ms.social.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ms.social.interfaces.ClickAddPostInterface;
import com.ms.social.fragments.CreatePostFragment;
import com.ms.social.fragments.DiscoverFragment;
import com.ms.social.fragments.EditProfileFragment;
import com.ms.social.fragments.FollowersFragment;
import com.ms.social.fragments.FollowingFragment;
import com.ms.social.fragments.HomeFragment;
import com.ms.social.R;
import com.ms.social.fragments.ProfileFragment;
import com.ms.social.fragments.SavedPostesFragment;
import com.squareup.picasso.Picasso;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class HomeActivity extends AppCompatActivity implements ClickAddPostInterface {
    FirebaseAuth fauth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference reference;
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        fauth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        bottomNavigationView = findViewById(R.id.nav_bottom_view);
        navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        TextView userName = view.findViewById(R.id.user_name);
        TextView bio = view.findViewById(R.id.bio);
        ImageView profileImage = view.findViewById(R.id.profile_image);
//        Log.i("tag", user.getPhotoUrl().toString());
        reference.child(COLLECTION_USERS)
                .child(user.getUid())
                .child(USER_PROFILE_PICTURE).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(HomeActivity.this).load(uri).fit().centerInside().into(profileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("tag", e.getMessage());
            }
        });
        userName.setText(user.getDisplayName());
        db.collection(COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){

                        if (documentSnapshot.getId().equals(user.getUid())) {
                            bio.setText((String) documentSnapshot.get("bio"));
                        }

                    }


                } else {
                    Log.i("tag", task.getException().getMessage());
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
                switch (item.getItemId()) {
                    case R.id.home:
                        setFragment(new HomeFragment(), "Home");
                        navigationView.getMenu().getItem(0).setChecked(true);
                        break;
                    case R.id.profile:
                        setFragment(new ProfileFragment(), "Profile");
                        navigationView.getMenu().getItem(5).setChecked(true);
                        break;
                    case R.id.discover:
                        setFragment(new DiscoverFragment(), "Discover");
                        navigationView.getMenu().getItem(1).setChecked(true);
                        break;
                    case R.id.create_post:
                        setFragment(new CreatePostFragment(), "Create Post");
                        navigationView.getMenu().getItem(2).setChecked(true);
                        break;
                }

                return true;
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawer(GravityCompat.START);
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
                switch (item.getItemId()){
                    case R.id.home:
                        setFragment(new HomeFragment(), "Home");
                        bottomNavigationView.getMenu().getItem(0).setChecked(true);
                        break;
                    case R.id.profile:
                        setFragment(new ProfileFragment(), "Profile");
                        bottomNavigationView.getMenu().getItem(3).setChecked(true);
                        break;
                    case R.id.discover:
                        setFragment(new DiscoverFragment(), "Discover");
                        bottomNavigationView.getMenu().getItem(1).setChecked(true);
                        break;
                    case R.id.create_post:
                        setFragment(new CreatePostFragment(), "Create Post");
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        break;
                    case R.id.followers:
                        setFragment(new FollowersFragment(), "Followers");
                        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                        break;
                    case R.id.following:
                        setFragment(new FollowingFragment(), "Following");
                        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                        break;
                    case R.id.edit_profile:
                        setFragment(new EditProfileFragment(), "Edit Profile");
                        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                        break;
                    case R.id.saved_posts:
                        setFragment(new SavedPostesFragment(), "Saved Posts");
                        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                        break;
                }
                return true;
            }
        });
        setFragment(new HomeFragment(), "Home");
        navigationView.getMenu().getItem(0).setChecked(true);




}
    private void setFragment(Fragment fragment, String title){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.coordinator_layout, fragment);
        getSupportActionBar().setTitle(title);
        fragmentTransaction.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.verify_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                HomeActivity.this.finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickAddPost() {
        setFragment(new HomeFragment(), "Home");
        navigationView.getMenu().getItem(0).setChecked(true);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
    }

}