package com.bandito.folksets.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;
import com.bandito.folksets.sql.entities.SetEntity;

import java.util.ArrayList;
import java.util.List;

public class SetListRecyclerViewAdapter extends RecyclerView.Adapter<SetListRecyclerViewAdapter.SetViewHolder> {
    private static final String TAG = SetListRecyclerViewAdapter.class.getName();
    private List<SetEntity> setEntityList = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public SetListRecyclerViewAdapter() {
    }

    public List<SetEntity> getSetEntityList() {
        return setEntityList;
    }
    public void setSetEntityList(List<SetEntity> setEntityList) {
        this.setEntityList = setEntityList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_item, viewGroup, false);
        return new SetViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull SetViewHolder viewHolder, final int position) {
        try {
            viewHolder.getSetNameTextView().setText(setEntityList.get(position).setName);
        } catch (Exception e) {
            Log.w(TAG, "An error occured while binding ViewHolder " + position + ".");
        }
    }

    @Override
    public int getItemCount() {
        return setEntityList.size();
    }

    // convenience method for getting data at click position
    public Long getItem(int id) {
        return setEntityList.get(id).setId;
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

    public class SetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView setNameTextView;

        public SetViewHolder(View view) {
            super(view);
            setNameTextView = view.findViewById(R.id.item_text);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public TextView getSetNameTextView() {
            return setNameTextView;
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
