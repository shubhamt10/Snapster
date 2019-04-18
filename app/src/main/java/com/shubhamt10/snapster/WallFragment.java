package com.shubhamt10.snapster;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class WallFragment extends Fragment {

    private ArrayList<Post> posts;
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference postsReference;
    private DocumentReference reference;
    private Post post;
    private boolean isLiked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wall,container,false);
        recyclerView = view.findViewById(R.id.wallRecycleView);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        postsReference = firestore.collection("posts");

        final String uid = firebaseAuth.getCurrentUser().getUid();

        if (posts != null) {
            if (!posts.isEmpty()) {
                final ArrayList<String> postUrls = new ArrayList<>();
                for (Post post: posts){
                    postUrls.add(post.getUrl());
                }
                PostRecycleViewAdapter.RecyclerViewClickListener clickListener = new PostRecycleViewAdapter.RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        final PostRecycleViewAdapter.PostViewHolder holder = (PostRecycleViewAdapter.PostViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        postsReference.whereEqualTo("url", postUrls.get(position)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                final DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                post = document.toObject(Post.class);
                                reference = postsReference.document(document.getId());
                                isLiked = post.getLikes().contains(uid);
                                if (isLiked) {
                                    holder.likeButton.setBackgroundColor(Color.GRAY);
                                    holder.likeButton.setText(R.string.like);
                                    holder.likeButton.setTextColor(Color.WHITE);
                                    reference.update("likes", FieldValue.arrayRemove(uid))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    System.out.println("unliked");
                                                    holder.likes.setText(String.valueOf(post.getLikes().size() - 1));
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    });
                                } else {
                                    holder.likeButton.setBackgroundColor(Color.RED);
                                    holder.likeButton.setText(R.string.unlike);
                                    holder.likeButton.setTextColor(Color.WHITE);
                                    reference.update("likes", FieldValue.arrayUnion(uid))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    System.out.println("liked");
                                                    holder.likes.setText(String.valueOf(post.getLikes().size() + 1));
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
                    }
                };
                PostRecycleViewAdapter adapter = new PostRecycleViewAdapter(getContext(), posts, clickListener);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        }else {
            System.out.println("Posts empty");
        }

        return view;
    }

    public void setPosts(ArrayList<Post> posts){
        this.posts = posts;
    }

}
