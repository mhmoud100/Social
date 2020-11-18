package com.ms.social.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ms.social.PostsFragment;
import com.ms.social.R;
import com.ms.social.PostAdapter;
import com.ms.social.UsersFragment;
import com.ms.social.model.Post;

import java.util.ArrayList;
import java.util.Objects;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.ms.social.help.Helper.COLLECTION_POSTS;

public class DiscoverFragment extends Fragment {
    private static final int PAGER_LAYOUTS_COUNT = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStatePagerAdapter(
                Objects.requireNonNull(getFragmentManager()), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0 : return new PostsFragment();
                    case 1 : return new UsersFragment();
                }
                return new PostsFragment();
            }
            @Override
            public int getCount() {
                return PAGER_LAYOUTS_COUNT;
            }
            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case 0 : return "Posts";
                    case 1 : return "Users";
                }
                return super.getPageTitle(position);
            }
        });

        tabLayout.setupWithViewPager(viewPager);


        return view;
    }
}