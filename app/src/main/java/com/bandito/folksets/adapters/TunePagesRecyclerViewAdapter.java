package com.bandito.folksets.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;

import java.util.List;

public class TunePagesRecyclerViewAdapter extends RecyclerView.Adapter<TunePagesRecyclerViewAdapter.PageViewHolder> {
    private final List<Bitmap> bitmapList;

    public TunePagesRecyclerViewAdapter(List<Bitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_imageview_item, viewGroup, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder viewHolder, int position) {
        viewHolder.getImageView().setImageBitmap(bitmapList.get(position));
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }


    public class PageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.adapter_imageview_item_imageview);
        }

        public ImageView getImageView() {
            return imageView;
        }
    }
}
