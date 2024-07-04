package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.SET_ENTITY;
import static com.bandito.folksets.util.Constants.SONG_ID;
import static com.bandito.folksets.util.Constants.SONG_TITLES;
import static java.util.Objects.isNull;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.adapters.SongListArrayAdapter;
import com.bandito.folksets.adapters.SongListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetActivity extends AppCompatActivity {

    private Constants.SetOperation currentSetOperation;
    private SetEntity currentSet = new SetEntity();
    private EditText setNameEditText;
    private RecyclerView selectedSongsRecyclerView;
    private SongListRecyclerViewAdapter selectedSongListRecyclerViewAdapter;
    private View lastSelectedSongSelectedView = null;
    private Drawable lastSelectedSongSelectedDrawable = null;
    private Integer selectedSongSelectionPosition = null;
    private Context context;
    private Dialog dialog;

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

        //Prepare the RecyclerView and its adapter for selected songs
        selectedSongsRecyclerView = findViewById(R.id.selected_songs_recyclerview);
        selectedSongsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        selectedSongListRecyclerViewAdapter = new SongListRecyclerViewAdapter();
        selectedSongListRecyclerViewAdapter.setClickListener(selectedSongItemClickListener);

        //Retrieve the bundle message
        setNameEditText = findViewById(R.id.set_name_edittext);
        if (Constants.SetOperation.editSet.toString().equals(getIntent().getExtras().getString(OPERATION))) {
            currentSetOperation = Constants.SetOperation.editSet;
            currentSet = (SetEntity) getIntent().getExtras().getSerializable(SET_ENTITY);
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.edit_set);
            setNameEditText.setText(currentSet.setName);
            String[] currentSetSongIdArrayOfStrings = StringUtils.split(currentSet.setSongs, DEFAULT_SEPARATOR);
            if (!isNull(currentSetSongIdArrayOfStrings)) {
                try {
                    List<SongEntity> unorderedCurrentSetSongEntityList = DatabaseManager.findSongsByIdInDatabase(SONG_ID + "," + SONG_TITLES, currentSetSongIdArrayOfStrings, null, null);
                    List<SongEntity> orderedCurrentSetSongEntityList = new ArrayList<>();
                    for (String songId : currentSetSongIdArrayOfStrings) {
                        orderedCurrentSetSongEntityList.add(unorderedCurrentSetSongEntityList.stream().filter(songEntity -> songEntity.songId.equals(Long.valueOf(songId))).findFirst().get());
                    }
                    selectedSongListRecyclerViewAdapter.setSongEntityList(orderedCurrentSetSongEntityList);
                } catch (FolkSetsException e) {
                    ExceptionManager.manageException(this, e);
                }
            }
        } else {
            findViewById(R.id.button_delete_set).setVisibility(View.GONE);
            currentSetOperation = Constants.SetOperation.createSet;
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.create_new_set);
        }

        selectedSongsRecyclerView.setAdapter(selectedSongListRecyclerViewAdapter);
    }

    private int getIndexOfSongIdInListOfSongEntities(String songId, List<SongEntity> songEntityList) throws FolkSetsException {
        for (int i = 0, max = songEntityList.size(); i < max; i++) {
            if (songEntityList.get(i).songId.equals(Long.valueOf(songId))) {
                return i;
            }
        }
        throw new FolkSetsException("An error occured while retrieving the index of a song id in a song list.", null);
    }

    protected void onSaveInstanceState (@NonNull Bundle outState) {
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
            ExceptionManager.manageException(this, e);
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
                DEFAULT_SEPARATOR
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
            ExceptionManager.manageException(this, e);
        }
    }

    private void selectSong(SongEntity songEntity) {
        selectedSongListRecyclerViewAdapter.addSongEntity(songEntity);
        selectedSongListRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void deselectSong(View view) {
        if (!isNull(selectedSongSelectionPosition)) {
            lastSelectedSongSelectedView.setBackground(lastSelectedSongSelectedDrawable);
            selectedSongListRecyclerViewAdapter.removeSongEntity(selectedSongSelectionPosition);
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

    public void openSelectSongDialog(View view) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.searchable_spinner);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        ListView listView=dialog.findViewById(R.id.searchable_spinner_list_view);
        SongListArrayAdapter arrayAdapter = new SongListArrayAdapter(this, android.R.layout.simple_list_item_1, StaticData.songEntityList);
        listView.setAdapter(arrayAdapter);
        ((EditText)dialog.findViewById(R.id.searchable_spinner_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectSong(arrayAdapter.getItem(position));
                dialog.dismiss();
            }
        });
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
