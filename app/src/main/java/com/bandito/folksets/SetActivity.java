package com.bandito.folksets;

import static java.util.Objects.isNull;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.adapters.SongListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetActivity extends AppCompatActivity {

    private static final String TAG = SetActivity.class.getName();
    private Constants.SetOperation currentSetOperation;
    private SetEntity currentSet = new SetEntity();
    private EditText setNameEditText;
    private SongListRecyclerViewAdapter availableSongListRecyclerViewAdapter;
    private View lastAvalaibleSongSelectedView = null;
    private Drawable lastAvalaibleSongSelectedDrawable = null;
    private Integer avalaibleSongSelectionPosition = null;
    private RecyclerView selectedSongsRecyclerView;
    private SongListRecyclerViewAdapter selectedSongListRecyclerViewAdapter;
    private View lastSelectedSongSelectedView = null;
    private Drawable lastSelectedSongSelectedDrawable = null;
    private Integer selectedSongSelectionPosition = null;
    private Context context;
    private final SongListRecyclerViewAdapter.ItemClickListener availableSongItemClickListener = new SongListRecyclerViewAdapter.ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (view.equals(lastAvalaibleSongSelectedView)) {
                lastAvalaibleSongSelectedView.setBackground(lastAvalaibleSongSelectedDrawable);
                lastAvalaibleSongSelectedView = null;
                lastAvalaibleSongSelectedDrawable = null;
                avalaibleSongSelectionPosition = null;
            } else {
                if (!isNull(lastAvalaibleSongSelectedView)) {
                    lastAvalaibleSongSelectedView.setBackground(lastAvalaibleSongSelectedDrawable);
                }
                lastAvalaibleSongSelectedView = view;
                lastAvalaibleSongSelectedDrawable = view.getBackground();
                avalaibleSongSelectionPosition = position;
                view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.view_border, context.getTheme()));
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            onItemClick(view, position);
        }
    };

    private final SongListRecyclerViewAdapter.ItemClickListener selectedSongItemClickListener = new SongListRecyclerViewAdapter.ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (view.equals(lastSelectedSongSelectedView)) {
                lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
                lastSelectedSongSelectedView = null;
                lastSelectedSongSelectedDrawable = null;
                selectedSongSelectionPosition = null;
            } else {
                if (!isNull(lastSelectedSongSelectedView)) {
                    lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
                }
                lastSelectedSongSelectedView = view;
                lastSelectedSongSelectedDrawable = view.getBackground();
                selectedSongSelectionPosition = position;
                view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.view_border, context.getTheme()));
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            onItemClick(view, position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.set), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = this;

        //Prepare the RecyclerView and its adapter for available songs
        RecyclerView availableSongsRecyclerView = findViewById(R.id.available_songs_recyclerview);
        availableSongsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        availableSongListRecyclerViewAdapter = new SongListRecyclerViewAdapter();
        availableSongListRecyclerViewAdapter.setClickListener(availableSongItemClickListener);

        //Prepare the RecyclerView and its adapter for selected songs
        selectedSongsRecyclerView = findViewById(R.id.selected_songs_recyclerview);
        selectedSongsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        selectedSongListRecyclerViewAdapter = new SongListRecyclerViewAdapter();
        selectedSongListRecyclerViewAdapter.setClickListener(selectedSongItemClickListener);

        //Get all available songs
        List<SongEntity> allSongs = new ArrayList<>();
        try {
            allSongs = DatabaseManager.findSongsWithValueInListInDatabase(Constants.SONG_ID + "," + Constants.SONG_TITLES, null, null, Constants.SONG_TITLES, null);
        } catch (FolkSetsException e) {
            Log.e(TAG, "An error occured when retrieving the list of all songs.", e);
        }

        //Retrieve the bundle message
        setNameEditText = findViewById(R.id.set_name_edittext);
        if (Constants.SetOperation.editSet.toString().equals(getIntent().getExtras().getString(Constants.OPERATION))) {
            currentSetOperation = Constants.SetOperation.editSet;
            currentSet = (SetEntity) getIntent().getExtras().getSerializable(Constants.SET_ENTITY);
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.edit_set);
            setNameEditText.setText(currentSet.setName);
            String[] currentSetSongIdArrayOfStrings = StringUtils.split(currentSet.setSongs, Constants.DEFAULT_SEPARATOR);
            if (!isNull(currentSetSongIdArrayOfStrings)) {
                List<Long> currentSetSongIdArrayOfLongs = Arrays.stream(currentSetSongIdArrayOfStrings).map(songIdStr -> Long.valueOf(songIdStr)).collect(Collectors.toList());
                List<SongEntity> selectedSong = new ArrayList<>();
                for(int i = allSongs.size() - 1; i >= 0; i--) {
                    if (currentSetSongIdArrayOfLongs.contains(allSongs.get(i).songId)) {
                        selectedSong.add(allSongs.remove(i));
                    }
                }
                selectedSongListRecyclerViewAdapter.setSongEntityList(selectedSong);
            }
        } else {
            findViewById(R.id.button_delete_set).setVisibility(View.GONE);
            currentSetOperation = Constants.SetOperation.createSet;
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.create_new_set);
        }
        availableSongListRecyclerViewAdapter.setSongEntityList(allSongs);

        availableSongsRecyclerView.setAdapter(availableSongListRecyclerViewAdapter);
        selectedSongsRecyclerView.setAdapter(selectedSongListRecyclerViewAdapter);
    }

    protected void onSaveInstanceState (Bundle outState) {
        //TODO: Save my data
        super.onSaveInstanceState(outState);
    }

    public void onBackButtonClick(View view) {
        this.finish();
    }

    public void onDeleteSetButtonClick(View view) {
        try {
            DatabaseManager.removeSetFromDatabase(currentSet.setId);
            Toast.makeText(this, "Set deleted", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    public void onSaveSetButtonClick(View view) {
        currentSet.setName = setNameEditText.getText().toString();
        if (currentSet.setName.isEmpty()) {
            Toast.makeText(this, "Choose a name for the set", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSet.setSongs = StringUtils.join(
                selectedSongListRecyclerViewAdapter.getSongEntityList().stream().map(songEntity -> songEntity.songId).collect(Collectors.toList()),
                Constants.DEFAULT_SEPARATOR
        );
        if (currentSet.setSongs.isEmpty()) {
            Toast.makeText(this, "Select at least one song", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (currentSetOperation == Constants.SetOperation.createSet) {
                DatabaseManager.insertSetInDatabase(currentSet);
            } else {
                DatabaseManager.updateSetInDatabase(currentSet);
            }
            Toast.makeText(this, "Set saved", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    public void selectSong(View view) {
        if (!isNull(avalaibleSongSelectionPosition)) {
            lastAvalaibleSongSelectedView.setBackground(lastAvalaibleSongSelectedDrawable);
            selectedSongListRecyclerViewAdapter.addSongEntity(availableSongListRecyclerViewAdapter.removeSongEntity(avalaibleSongSelectionPosition));
            availableSongListRecyclerViewAdapter.notifyDataSetChanged();
            selectedSongListRecyclerViewAdapter.notifyDataSetChanged();
            lastAvalaibleSongSelectedView = null;
            lastAvalaibleSongSelectedDrawable = null;
            avalaibleSongSelectionPosition = null;
        }
    }

    public void deselectSong(View view) {
        if (!isNull(selectedSongSelectionPosition)) {
            lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
            availableSongListRecyclerViewAdapter.addSongEntity(selectedSongListRecyclerViewAdapter.removeSongEntity(selectedSongSelectionPosition));
            availableSongListRecyclerViewAdapter.notifyDataSetChanged();
            selectedSongListRecyclerViewAdapter.notifyDataSetChanged();
            lastSelectedSongSelectedView = null;
            lastSelectedSongSelectedDrawable = null;
            selectedSongSelectionPosition = null;
        }
    }

    public void moveSongUp(View view) {
        if (!isNull(selectedSongSelectionPosition) && selectedSongSelectionPosition > 0) {
            lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
            selectedSongListRecyclerViewAdapter.addSongEntityAtIndex(selectedSongSelectionPosition - 1, selectedSongListRecyclerViewAdapter.removeSongEntity(selectedSongSelectionPosition));
            selectedSongListRecyclerViewAdapter.notifyDataSetChanged();
            lastSelectedSongSelectedView = null;
            lastSelectedSongSelectedDrawable = null;
            int newPosition = selectedSongSelectionPosition - 1;
            selectedSongSelectionPosition = null;
            selectedSongItemClickListener.onItemClick(selectedSongsRecyclerView.getChildAt(newPosition), newPosition);
        }
    }

    public void moveSongDown(View view) {
        if (!isNull(selectedSongSelectionPosition) && selectedSongSelectionPosition < selectedSongListRecyclerViewAdapter.getItemCount() - 1) {
            lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
            selectedSongListRecyclerViewAdapter.addSongEntityAtIndex(selectedSongSelectionPosition + 1, selectedSongListRecyclerViewAdapter.removeSongEntity(selectedSongSelectionPosition));
            selectedSongListRecyclerViewAdapter.notifyDataSetChanged();
            lastSelectedSongSelectedView = null;
            lastSelectedSongSelectedDrawable = null;
            int newPosition = selectedSongSelectionPosition + 1;
            selectedSongSelectionPosition = null;
            selectedSongItemClickListener.onItemClick(selectedSongsRecyclerView.getChildAt(newPosition), newPosition);
        }
    }

    //The following strange bit of code make it so EditText loose the focus when we touch outside them.
    //It applies even in fragments that are "children" of this activity.
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}