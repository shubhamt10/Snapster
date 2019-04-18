package com.shubhamt10.snapster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridRecycleViewAdapter extends RecyclerView.Adapter<GridRecycleViewAdapter.GridViewHolder>{

    private Context mContext;
    private ArrayList<String> imageUrls;
    private GridRecyclerViewClickListener mListener;

    public GridRecycleViewAdapter(Context context, ArrayList<String> imageUrls, GridRecyclerViewClickListener listener) {
        this.mContext = context;
        this.imageUrls = imageUrls;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_listitem,viewGroup,false);
        return new GridViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder gridViewHolder, int i) {

        String url = imageUrls.get(i);

        Glide.with(gridViewHolder.image.getContext())
                .asBitmap()
                .load(url)
                .into(gridViewHolder.image);

    }

    public interface GridRecyclerViewClickListener {
        void onClick(View view, int position);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView image;
        private GridRecyclerViewClickListener gListener;

        public GridViewHolder(@NonNull View itemView, GridRecyclerViewClickListener listener) {
            super(itemView);

            image = itemView.findViewById(R.id.gridImage);
            gListener = listener;
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
           gListener.onClick(view,getAdapterPosition());
        }
    }

}
