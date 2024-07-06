package com.bandito.folksets.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;
import com.bandito.folksets.sql.entities.SongEntity;

import java.util.ArrayList;
import java.util.List;

public class SongListRecyclerViewAdapter extends RecyclerView.Adapter<SongListRecyclerViewAdapter.SongViewHolder> {
    private static final String TAG = SongListRecyclerViewAdapter.class.getName();
    private List<SongEntity> songEntityList = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public SongListRecyclerViewAdapter() {
    }


    public List<SongEntity> getSongEntityList() {
        return songEntityList;
    }
    public void setSongEntityList(List<SongEntity> songEntityList) {
        this.songEntityList = songEntityList;
    }

    public SongEntity removeSongEntity(int position) {
        return songEntityList.remove(position);
    }

    public void addSongEntity(SongEntity songEntity) {
        songEntityList.add(songEntity);
    }

    public void addSongEntityAtIndex(int index, SongEntity songEntity) {
        songEntityList.add(index, songEntity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_textview_item, viewGroup, false);
        return new SongViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder viewHolder, final int position) {
        try {
            viewHolder.getSongTitleTextView().setText(songEntityList.get(position).getFirstTitle());
        } catch (Exception e) {
            Log.w(TAG, "An error occured while binding ViewHolder " + position + ".");
        }
    }

    @Override
    public int getItemCount() {
        return songEntityList.size();
    }

    // convenience method for getting data at click position
    public Long getItem(int id) {
        return songEntityList.get(id).songId;
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onLongItemClick(View view, int position);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView songTitleTextView;

        public SongViewHolder(View view) {
            super(view);
            songTitleTextView = view.findViewById(R.id.item_text);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public TextView getSongTitleTextView() {
            return songTitleTextView;
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onLongItemClick(view, getAdapterPosition());
                return true;
            } else {
                return false;
            }
        }
    }
}
