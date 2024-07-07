package com.bandito.folksets.adapters;

import static android.view.Menu.NONE;
import static com.bandito.folksets.util.Constants.CLICK_TYPE;
import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.POSITION;
import static com.bandito.folksets.util.Constants.SET_ENTITY;
import static com.bandito.folksets.util.Constants.SONG_ID;
import static com.bandito.folksets.util.Constants.SONG_TITLES;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.R;
import com.bandito.folksets.SongActivity;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SetListRecyclerViewAdapter extends RecyclerView.Adapter<SetListRecyclerViewAdapter.SetViewHolder> {
    private static final String TAG = SetListRecyclerViewAdapter.class.getName();
    private List<SetEntity> setEntityList = new ArrayList<>();
    private ItemClickListener itemClickListener;
    private final Activity activity;
    private final Context context;

    public SetListRecyclerViewAdapter(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
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
                .inflate(R.layout.adapter_set_item, viewGroup, false);
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
        private List<SongEntity> setSongEntityList;

        public SetViewHolder(View view) {
            super(view);
            setNameTextView = view.findViewById(R.id.item_set_name_textview);
            view.findViewById(R.id.item_set_floatingActionButton).setOnClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public TextView getSetNameTextView() {
            return setNameTextView;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.item_set_floatingActionButton) {
                displayPopupMenuOfTunesInSet(view);
            } else if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
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

        private void displayPopupMenuOfTunesInSet(View view) {
            try {
                String[] songIdArray = StringUtils.split(setEntityList.get(getAdapterPosition()).setSongs, DEFAULT_SEPARATOR);
                List<SongEntity> unorderedSongEntityList = DatabaseManager.findSongsByIdInDatabase(
                        SONG_ID + "," + SONG_TITLES,
                        songIdArray,
                        null,
                        null);
                setSongEntityList = Utilities.rearangeSongInSetOrder(unorderedSongEntityList, songIdArray);
                PopupMenu popupMenu = new PopupMenu(context, view);
                for (int i = 0, max = setSongEntityList.size(); i < max; i++) {
                    popupMenu.getMenu().add(NONE, i, NONE, setSongEntityList.get(i).getFirstTitle());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Utilities.loadActivity(activity, context, SongActivity.class, new Pair[]{
                                new Pair<>(OPERATION, Constants.SongOrSet.set.toString()),
                                new Pair<>(POSITION, menuItem.getItemId()),
                                new Pair<>(SET_ENTITY, setEntityList.get(getAdapterPosition())),
                                new Pair<>(CLICK_TYPE, Constants.ClickType.shortClick.toString())
                        });
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            } catch (Exception e) {
                ExceptionManager.manageException(context, e);
            }
        }
    }
}
