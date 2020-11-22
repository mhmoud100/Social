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
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.ms.social.interfaces.ClickGotoEditInterface;
import com.ms.social.interfaces.ClickGotoFollowersInterface;
import com.ms.social.interfaces.ClickGotoFollowingInterface;
import com.squareup.picasso.Picasso;

import static com.ms.social.help.Helper.COLLECTION_USERS;
import static com.ms.social.help.Helper.USER_PROFILE_PICTURE;

public class HomeActivity extends AppCompatActivity implements ClickAddPostInterface, ClickGotoFollowingInterface, ClickGotoFollowersInterface, ClickGotoEditInterface {

    FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        bottomNavigationView = findViewById(R.id.nav_bottom_view);
        navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        TextView userName = view.findViewById(R.id.user_name);
        TextView bio = view.findViewById(R.id.bio);
        ImageView profileImage = view.findViewById(R.id.profile_image);

        if (user.getPhotoUrl() != null) {
            Picasso.with(this).load(user.getPhotoUrl()).fit().centerCrop().into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_account_circle);
        }
        userName.setText(user.getDisplayName());
        db.collection(COLLECTION_USERS).document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if ( task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    bio.setText((String) doc.get("bio"));
                }else {
                    Log.i("tag", "Failed", task.getException());
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
        getMenuInflater().inflate(R.menu.home_options_menu, menu);
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

    @Override
    public void onClickGotoEdit() {
        setFragment(new EditProfileFragment(), "Edit Profile");
        navigationView.getMenu().getItem(6).setChecked(true);
        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
    }

    @Override
    public void onClickGotoFollowers() {
        setFragment(new FollowersFragment(), "Followers");
        navigationView.getMenu().getItem(3).setChecked(true);
        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
    }

    @Override
    public void onClickGotoFollowing() {
        setFragment(new FollowingFragment(), "Following");
        navigationView.getMenu().getItem(4).setChecked(true);
        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
    }
}