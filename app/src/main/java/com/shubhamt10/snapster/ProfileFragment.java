package com.shubhamt10.snapster;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {

    private User user;
    private ImageView displayPicture;
    private TextView displayName;
    private TextView bioTextView;
    private TextView followersTextView;
    private TextView followingTextView;
    private RecyclerView gridRecyclerView;
    private Button editProfileButton;
    private TextView noPosts;

    private FirebaseFirestore firestore;
    private CollectionReference postsReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        firestore = FirebaseFirestore.getInstance();
        postsReference = firestore.collection("posts");

        displayPicture = view.findViewById(R.id.displayPicture);
        displayName = view.findViewById(R.id.nameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        followersTextView = view.findViewById(R.id.followers);
        followingTextView = view.findViewById(R.id.following);
        gridRecyclerView = view.findViewById(R.id.gridRecyclerView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        noPosts = view.findViewById(R.id.noPostsYet);

        Glide.with(displayPicture.getContext())
                .asBitmap()
                .load(user.getDisplayPictureUrl())
                .into(displayPicture);

        displayName.setText(user.getName());

        if (!user.getBio().isEmpty()) {
            bioTextView.setText(user.getBio());
        }

        if (user.getFollowers().isEmpty()){
            followersTextView.setText("0");
        }else {
            String followers = Integer.toString(user.getFollowers().size());
            followersTextView.setText(followers);
        }

        if (user.getFollowing().isEmpty()){
            followingTextView.setText("0");
        }else {
            String following = Integer.toString(user.getFollowing().size());
            followingTextView.setText(following);
        }

        if (!user.getPostUrls().isEmpty()) {
            noPosts.setVisibility(View.GONE);
            GridRecycleViewAdapter.GridRecyclerViewClickListener gridRecyclerViewClickListener = new GridRecycleViewAdapter.GridRecyclerViewClickListener() {
                @Override
                public void onClick(View view, int position) {
                    try {
                        System.out.println(position);
                        postsReference.whereEqualTo("url",user.getPostUrls().get(position)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Post post = queryDocumentSnapshots.getDocuments().get(0).toObject(Post.class);
                                PostFragment fragment = new PostFragment();
                                fragment.setPost(post);
                                getFragmentManager().beginTransaction().replace(R.id.fragments_container,fragment).addToBackStack(null).commit();
                            }
                        });
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            };
            GridRecycleViewAdapter adapter = new GridRecycleViewAdapter(getContext(), user.getPostUrls(),gridRecyclerViewClickListener);
            gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            gridRecyclerView.setAdapter(adapter);
        }

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditFragment fragment = new EditFragment();
                fragment.setUser(user);
                getFragmentManager().beginTransaction().replace(R.id.fragments_container,fragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
