package com.shubhamt10.snapster;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class PostRecycleViewAdapter extends RecyclerView.Adapter<PostRecycleViewAdapter.PostViewHolder>{

    private Context mContext;
    private ArrayList<Post> posts ;
    private FirebaseAuth firebaseAuth;
    private Post post;
    private int likesSize;
    private RecyclerViewClickListener aListener;

    public PostRecycleViewAdapter(Context mContext, ArrayList<Post> posts,RecyclerViewClickListener listener) {
        this.mContext = mContext;
        this.posts = posts;
        this.aListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.wall_listitem,viewGroup,false);
        firebaseAuth = FirebaseAuth.getInstance();

        return new PostViewHolder(view,aListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder postViewHolder, int i) {
        post = posts.get(i);

        likesSize = post.getLikes().size();
        String likes = Integer.toString(likesSize);
        postViewHolder.postUsername.setText(post.getUsername());
        postViewHolder.likes.setText(likes);
        postViewHolder.postCaption.setText(post.getCaption());

        Glide.with(mContext)
                .asBitmap()
                .load(post.getUrl())
                .into(postViewHolder.postImage);


        if (post.getLikes().contains(firebaseAuth.getCurrentUser().getUid())){
                postViewHolder.likeButton.setBackgroundResource(R.drawable.ic_unlike_button);
        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView postImage;
        public TextView postUsername;
        public TextView likes;
        public TextView postCaption;
        public Button likeButton;
        private RecyclerViewClickListener mListener;

        @Override
        public void onClick(View view) {
            mListener.onClick(view,getAdapterPosition());
        }

        public PostViewHolder(@NonNull View itemView, RecyclerViewClickListener listener) {
            super(itemView);

            postImage = itemView.findViewById(R.id.postImageView);
            postUsername = itemView.findViewById(R.id.postUsernameText);
            likes = itemView.findViewById(R.id.likes);
            postCaption = itemView.findViewById(R.id.postCaption);
            likeButton = itemView.findViewById(R.id.likeButton);
            mListener = listener;
            itemView.setOnClickListener(this);
            likeButton.setOnClickListener(this);

        }
    }
}
