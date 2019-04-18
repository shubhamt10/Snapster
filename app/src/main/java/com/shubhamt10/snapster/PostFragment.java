package com.shubhamt10.snapster;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class PostFragment extends Fragment {

    private Post post;
    private boolean isLiked;

    private ImageView postImage;
    private TextView usernameText;
    private TextView postLikes;
    private TextView caption;
    private Button postLikeButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference postsReference;
    private DocumentSnapshot snapshot;
    private DocumentReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post,container,false);



        firebaseAuth = FirebaseAuth.getInstance();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        postsReference = firestore.collection("posts");

        postImage = view.findViewById(R.id.postImage);
        usernameText = view.findViewById(R.id.postUsername);
        postLikes = view.findViewById(R.id.postLikes);
        caption = view.findViewById(R.id.postCptn);
        postLikeButton = view.findViewById(R.id.postLikeButton);

        Glide.with(getActivity().getApplicationContext())
                .asBitmap()
                .load(post.getUrl())
                .into(postImage);

        usernameText.setText(post.getUsername());
        postLikes.setText(String.valueOf(post.getLikes().size()));
        caption.setText(post.getCaption());

        postsReference.whereEqualTo("url",post.getUrl()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                snapshot = queryDocumentSnapshots.getDocuments().get(0);
                reference = postsReference.document(snapshot.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });

        isLiked = post.getLikes().contains(uid);
        if (isLiked){
            postLikeButton.setBackgroundResource(R.drawable.ic_unlike_button);
        }

        postLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLiked){
                    postLikeButton.setBackgroundResource(R.drawable.ic_like_button);
                    reference.update("likes", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postLikes.setText(String.valueOf(post.getLikes().size()+1));
                                    isLiked = true;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getMessage());
                                }
                    });
                }else {
                    postLikeButton.setBackgroundResource(R.drawable.ic_unlike_button);
                    reference.update("likes", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postLikes.setText(String.valueOf(post.getLikes().size()-1));
                                    isLiked = false;
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



    public void setPost(Post post){
        this.post = post;
    }
}
