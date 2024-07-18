package com.bandito.folksets.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TuneSelectorRecyclerViewAdapter extends RecyclerView.Adapter<TuneSelectorRecyclerViewAdapter.TuneViewHolder> {

    private static final String TAG = TuneSelectorRecyclerViewAdapter.class.getName();
    private final Activity activity;
    private final Context context;
    private final Constants.ManagementOperation managementOperation;
    private final int purple;
    private final int white;
    private List<TuneEntity> tuneEntityList = new ArrayList<>();
    private String currentTagOrPlayer;

    private TuneSelectorRecyclerViewAdapter.ItemClickListener itemClickListener;

    public TuneSelectorRecyclerViewAdapter(Activity activity, Context context, Constants.ManagementOperation managementOperation) {
        this.activity = activity;
        this.context = context;
        this.managementOperation = managementOperation;
        this.purple = ContextCompat.getColor(context, R.color.purple);
        this.white = ContextCompat.getColor(context, R.color.white);
    }

    public List<TuneEntity> getTuneEntityList() {
        return tuneEntityList;
    }

    public void setTuneEntityList(List<TuneEntity> tuneEntityList) {
        this.tuneEntityList = tuneEntityList;
    }

    public void setCurrentTagOrPlayer(String currentTagOrPlayer) {
        this.currentTagOrPlayer = currentTagOrPlayer;
    }

    public void setClickListener(TuneSelectorRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TuneSelectorRecyclerViewAdapter.TuneViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_textview_item, viewGroup, false);
        return new TuneSelectorRecyclerViewAdapter.TuneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TuneSelectorRecyclerViewAdapter.TuneViewHolder viewHolder, int position) {
        try {
            TuneEntity tuneEntity = tuneEntityList.get(position);
            viewHolder.getTuneTitleTextView().setText(tuneEntity.getFirstTitle());
            if (!StringUtils.isEmpty(currentTagOrPlayer)) {
                if (managementOperation == Constants.ManagementOperation.manageTags) {
                    viewHolder.markAsSelected(tuneEntity.hasTag(currentTagOrPlayer));
                } else {
                    viewHolder.markAsSelected(tuneEntity.hasPlayer(currentTagOrPlayer));
                }
            }
        } catch (Exception e) {
            ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An error occured while binding ViewHolder " + position + ".", e));
        }
    }

    @Override
    public int getItemCount() {
        return tuneEntityList.size();
    }

    public interface ItemClickListener {
        void onItemClick(TuneViewHolder tuneViewHolder, View view, int position);
        void onLongItemClick(TuneViewHolder tuneViewHolder, View view, int position);
    }

    public class TuneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView tuneTitleTextView;
        private final CardView cardView;

        public TuneViewHolder(View view) {
            super(view);
            tuneTitleTextView = view.findViewById(R.id.adapter_textview_item_textview);
            cardView = view.findViewById(R.id.adapter_textview_cardview);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void markAsSelected(boolean selected) {
            cardView.setCardBackgroundColor(selected ? purple : white);
        }
        public TextView getTuneTitleTextView() {
            return tuneTitleTextView;
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(this, view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onLongItemClick(this, view, getAdapterPosition());
            }
            return true;
        }
    }
}
