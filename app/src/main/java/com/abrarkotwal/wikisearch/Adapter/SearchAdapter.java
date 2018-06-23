package com.abrarkotwal.wikisearch.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abrarkotwal.wikisearch.Activity.SinglePageInformationActivity;
import com.abrarkotwal.wikisearch.R;
import com.abrarkotwal.wikisearch.Adapter.Pojo.WikiData;
import com.bumptech.glide.Glide;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewItemHolder>{

    private Context context;
    private List<WikiData> wikiDataList;
    

    public SearchAdapter(Context context, List<WikiData> wikiDataList) {
        this.context = context;
        this.wikiDataList = wikiDataList;
    }

    @Override
    public ViewItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_suggesion, parent, false);
        return new ViewItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewItemHolder holder, int position) {
        final WikiData currentData = wikiDataList.get(position);

        holder.wikiTitle.setText(currentData.getTitle());
        holder.wikiDescription.setText(currentData.getDescription());

        if (currentData.getImagepath().equals("Not Found")){
            holder.wikiImage.setImageResource(R.drawable.ic_placeholder);
        }else{
            Glide.with(context).load(currentData.getImagepath()).into(holder.wikiImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SinglePageInformationActivity.class);
                intent.putExtra("pageId",currentData.getPageid());
                intent.putExtra("title",currentData.getTitle());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return wikiDataList.size();
    }

    public class ViewItemHolder extends RecyclerView.ViewHolder {

        public ImageView wikiImage;
        public TextView wikiTitle,wikiDescription;

        public ViewItemHolder(View itemView) {
            super(itemView);
            wikiImage= (ImageView) itemView.findViewById(R.id.wikiImage);
            wikiTitle= (TextView) itemView.findViewById(R.id.wikiTitle);
            wikiDescription= (TextView) itemView.findViewById(R.id.wikiDescription);
        }
    }
}
