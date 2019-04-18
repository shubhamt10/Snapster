package com.shubhamt10.snapster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SearchRecycleViewAdapter extends RecyclerView.Adapter<SearchRecycleViewAdapter.SearchViewHolder> {

    private Context mContext;
    private ArrayList<ResultUser> results ;
    private RecyclerViewClickListener mListener;

    public SearchRecycleViewAdapter(Context mContext, ArrayList<ResultUser> results,RecyclerViewClickListener listener) {
        this.mContext = mContext;
        this.results = results;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public SearchRecycleViewAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_listitem,viewGroup,false);
        return new SearchRecycleViewAdapter.SearchViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecycleViewAdapter.SearchViewHolder searchViewHolder, int i) {
        ResultUser user = results.get(i);

        searchViewHolder.searchUsername.setText(user.getUsername());
        searchViewHolder.searchName.setText(user.getName());

        Glide.with(mContext)
                .asBitmap()
                .load(user.getUrl())
                .into(searchViewHolder.searchImage);

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView searchImage;
        public TextView searchUsername;
        public TextView searchName;

        private RecyclerViewClickListener hListener;

        public SearchViewHolder(@NonNull View itemView, RecyclerViewClickListener listener) {
            super(itemView);

            searchImage = itemView.findViewById(R.id.searchDp);
            searchUsername = itemView.findViewById(R.id.searchUsername);
            searchName = itemView.findViewById(R.id.searchName);
            hListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            hListener.onClick(view,getAdapterPosition());
        }
    }
}
