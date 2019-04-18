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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    private User user;
    private ImageView displayPicture;
    private TextView displayName;
    private TextView bioTextView;
    private TextView followersTextView;
    private TextView followingTextView;
    private RecyclerView gridRecyclerView;
    private Button followButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference usersReference;
    private CollectionReference postsReference;
    private Boolean isFollowed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersReference = firestore.collection("users");
        postsReference = firestore.collection("posts");

        displayPicture = view.findViewById(R.id.displayPicture);
        displayName = view.findViewById(R.id.nameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        followersTextView = view.findViewById(R.id.followers);
        followingTextView = view.findViewById(R.id.following);
        gridRecyclerView = view.findViewById(R.id.gridRecyclerView);
        followButton = view.findViewById(R.id.followButton);

        for (String uid: user.getFollowers()){
            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                isFollowed = true;
                followButton.setText(R.string.unfollow);
                break;
            }
        }

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
                                getFragmentManager().beginTransaction().replace(R.id.fragments_container,fragment).commit();
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

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isFollowed) {
                    usersReference.whereEqualTo("uid", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            usersReference.document(document.getId())
                                    .update("followers", FieldValue.arrayRemove(firebaseAuth.getCurrentUser().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersReference.whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    DocumentSnapshot current = queryDocumentSnapshots.getDocuments().get(0);
                                                    usersReference.document(current.getId())
                                                            .update("following", FieldValue.arrayRemove(user.getUid()))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getContext(), "User Unfollowed", Toast.LENGTH_LONG).show();
                                                                    followButton.setText(R.string.follow);
                                                                    isFollowed = false;
                                                                    int followers = Integer.parseInt(followersTextView.getText().toString());
                                                                    String foll = Integer.toString(followers-1);
                                                                    followersTextView.setText(foll);
                                                                    try {
                                                                        ((HomeActivity)getActivity()).updatePosts();
                                                                        ((HomeActivity) getActivity()).updateUser();
                                                                    }catch (Exception e){
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            System.out.println(e.getMessage());
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println(e.getMessage());
                        }
                    });
                } else {
                    usersReference.whereEqualTo("uid", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            usersReference.document(document.getId())
                                    .update("followers", FieldValue.arrayUnion(firebaseAuth.getCurrentUser().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersReference.whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    DocumentSnapshot current = queryDocumentSnapshots.getDocuments().get(0);
                                                    usersReference.document(current.getId())
                                                            .update("following", FieldValue.arrayUnion(user.getUid()))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getContext(), "User Followed", Toast.LENGTH_LONG).show();
                                                                    followButton.setText(R.string.unfollow);
                                                                    isFollowed = true;
                                                                    int followers = Integer.parseInt(followersTextView.getText().toString());
                                                                    String foll = Integer.toString(followers+1);
                                                                    followersTextView.setText(foll);
                                                                    try {
                                                                        ((HomeActivity)getActivity()).updatePosts();
                                                                        ((HomeActivity) getActivity()).updateUser();
                                                                    }catch (Exception e){
                                                                        System.out.println(e.getMessage());
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            System.out.println(e.getMessage());
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println(e.getMessage());
                        }
                    });
                }
            }
        });
        return view;
    }
    public void setUser(User user) {
        this.user = user;
    }

}

