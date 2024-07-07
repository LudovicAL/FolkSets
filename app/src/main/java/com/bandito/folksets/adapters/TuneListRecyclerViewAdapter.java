package com.bandito.folksets.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;
import com.bandito.folksets.sql.entities.TuneEntity;

import java.util.ArrayList;
import java.util.List;

public class TuneListRecyclerViewAdapter extends RecyclerView.Adapter<TuneListRecyclerViewAdapter.TuneViewHolder> {
    private static final String TAG = TuneListRecyclerViewAdapter.class.getName();
    private List<TuneEntity> tuneEntityList = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public TuneListRecyclerViewAdapter() {
    }


    public List<TuneEntity> getTuneEntityList() {
        return tuneEntityList;
    }
    public void setTuneEntityList(List<TuneEntity> tuneEntityList) {
        this.tuneEntityList = tuneEntityList;
    }

    public TuneEntity removeTuneEntity(int position) {
        return tuneEntityList.remove(position);
    }

    public void addTuneEntity(TuneEntity tuneEntity) {
        tuneEntityList.add(tuneEntity);
    }

    public void addTuneEntityAtIndex(int index, TuneEntity tuneEntity) {
        tuneEntityList.add(index, tuneEntity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public TuneViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_textview_item, viewGroup, false);
        return new TuneViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull TuneViewHolder viewHolder, final int position) {
        try {
            viewHolder.getTuneTitleTextView().setText(tuneEntityList.get(position).getFirstTitle());
        } catch (Exception e) {
            Log.w(TAG, "An error occured while binding ViewHolder " + position + ".");
        }
    }

    @Override
    public int getItemCount() {
        return tuneEntityList.size();
    }

    // convenience method for getting data at click position
    public Long getItem(int id) {
        return tuneEntityList.get(id).tuneId;
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

    public class TuneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView tuneTitleTextView;

        public TuneViewHolder(View view) {
            super(view);
            tuneTitleTextView = view.findViewById(R.id.item_text);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public TextView getTuneTitleTextView() {
            return tuneTitleTextView;
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
