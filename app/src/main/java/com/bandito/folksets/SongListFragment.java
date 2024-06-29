package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.SONG_COMPOSER;
import static com.bandito.folksets.util.Constants.SONG_CONSULTATION_NUMBER;
import static com.bandito.folksets.util.Constants.SONG_FILE_CREATION_DATE;
import static com.bandito.folksets.util.Constants.SONG_ID;
import static com.bandito.folksets.util.Constants.SONG_KEY;
import static com.bandito.folksets.util.Constants.SONG_LAST_CONSULTATION_DATE;
import static com.bandito.folksets.util.Constants.SONG_PLAYED_BY;
import static com.bandito.folksets.util.Constants.SONG_REGION_OF_ORIGIN;
import static com.bandito.folksets.util.Constants.SONG_TAGS;
import static com.bandito.folksets.util.Constants.SONG_TITLES;
import static com.bandito.folksets.util.Constants.SORT_ASC;
import static com.bandito.folksets.util.Constants.SORT_DESC;
import static java.util.Objects.isNull;

import android.os.Bundle;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.bandito.folksets.adapters.SongListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.services.UpdateDatabaseThread;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongListFragment extends Fragment implements AdapterView.OnItemSelectedListener, SongListRecyclerViewAdapter.ItemClickListener {

    private ProgressBar progressBar;

    private Spinner sortSpinner;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private final MaterialButtonToggleGroup.OnButtonCheckedListener materialButtonToggleGroupCheckedListener = (group, checkedId, isChecked) -> {
        if (isChecked) {
            demandNewSearch(false);
        }
    };
    private Timer timer;
    private EditText editText;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            demandNewSearch(true);
        }
    };
    private ExecutorService executorService;
    private UpdateDatabaseThread updateDatabaseThread;
    public SongListRecyclerViewAdapter songListRecyclerViewAdapter;

    public SongListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);
        progressBar = view.findViewById(R.id.songListProgressBar);
        materialButtonToggleGroup = view.findViewById(R.id.songToggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(materialButtonToggleGroupCheckedListener);
        editText = view.findViewById(R.id.songListEditText);
        editText.addTextChangedListener(textWatcher);
        RecyclerView recyclerView = view.findViewById(R.id.songListRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        songListRecyclerViewAdapter = new SongListRecyclerViewAdapter();
        songListRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(songListRecyclerViewAdapter);
        sortSpinner = view.findViewById(R.id.song_sort_spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.song_sort_array,
                R.layout.spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(0,false);
        sortSpinner.setOnItemSelectedListener(this);
        return view;
    }

    private void demandNewSearch(boolean userIsTyping) {
        if (!isNull(timer)) {
            timer.cancel();
        }
        if (userIsTyping) {
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            requireActivity().runOnUiThread(() -> performSearch());
                        }
                    },
                    500L);
        } else {
            performSearch();
        }
    }

    private Pair<String, String> getSortParameters() throws FolkSetsException {
        String sortParameterStr = sortSpinner.getSelectedItem().toString();
        if (getString(R.string.sort_by_title).equals(sortParameterStr)) {
            return new Pair<>(SONG_TITLES, SORT_ASC);
        } else if (getString(R.string.sort_by_last_created).equals(sortParameterStr)) {
            return new Pair<>(SONG_FILE_CREATION_DATE, SORT_DESC);
        } else if (getString(R.string.sort_by_last_consulted).equals(sortParameterStr)) {
            return new Pair<>(SONG_LAST_CONSULTATION_DATE, SORT_DESC);
        } else if (getString(R.string.sort_by_most_consulted).equals(sortParameterStr)) {
            return new Pair<>(SONG_CONSULTATION_NUMBER, SORT_DESC);
        } else {
            throw new FolkSetsException("An unrecognized sort parameter was encountered.", null);
        }
    }

    private void performSearch() {
        try {
            String textToSearch = editText.getText().toString();
            Pair<String, String> sortParameters = getSortParameters();
            if (textToSearch.isEmpty()) {
                SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsInDatabase(SONG_ID + "," + SONG_TITLES, null, null, sortParameters.first, sortParameters.second);
            } else {
                int i = materialButtonToggleGroup.getCheckedButtonId();
                if (i == R.id.toggleButtonTitle) {
                    Log.i("Search", "Seaching title: " + textToSearch);
                    String[] titleArray = StringUtils.split(textToSearch, Constants.DEFAULT_SEPARATOR);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_TITLES, SONG_TITLES, titleArray, sortParameters.first, sortParameters.second);
                } else if (i == R.id.toggleButtonTag) {
                    Log.i("Search", "Seaching tag: " + textToSearch);
                    String[] tagArray = StringUtils.split(textToSearch, Constants.DEFAULT_SEPARATOR);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_TITLES, SONG_TAGS, tagArray, sortParameters.first, sortParameters.second);
                } else if (i == R.id.toggleButtonComposer) {
                    Log.i("Search", "Seaching composer: " + textToSearch);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsInDatabase(SONG_ID + "," + SONG_TITLES, SONG_COMPOSER, textToSearch, sortParameters.first, sortParameters.second);
                } else if (i == R.id.toggleButtonRegion) {
                    Log.i("Search", "Seaching region: " + textToSearch);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsInDatabase(SONG_ID + "," + SONG_TITLES, SONG_REGION_OF_ORIGIN, textToSearch, sortParameters.first, sortParameters.second);
                } else if (i == R.id.toggleButtonKey) {
                    Log.i("Search", "Seaching key: " + textToSearch);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsInDatabase(SONG_ID + "," + SONG_TITLES, SONG_KEY, textToSearch, sortParameters.first, sortParameters.second);
                } else if (i == R.id.toggleButtonPlayedBy) {
                    Log.i("Search", "Seaching played by: " + textToSearch);
                    String[] playedByArray = StringUtils.split(textToSearch, Constants.DEFAULT_SEPARATOR);
                    SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_TITLES, SONG_PLAYED_BY, playedByArray, sortParameters.first, sortParameters.second);
                }
            }
            songListRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            String selectedDirectoryUri = Utilities.readStringFromSharedPreferences(requireActivity(), Constants.STORAGE_DIRECTORY_URI, null);
            if (!isNull(selectedDirectoryUri)) {
                updateDatabaseThread = new UpdateDatabaseThread(requireActivity(), requireContext(), progressBar, songListRecyclerViewAdapter);
                executorService.execute(updateDatabaseThread);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isNull(updateDatabaseThread)) {
            updateDatabaseThread.interrupt();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            SongEntity songEntity = DatabaseManager.findSongsInDatabase("*", SONG_ID, String.valueOf(songListRecyclerViewAdapter.getItem(position)), null, null).get(0);
            songEntity.songConsultationNumber++;
            DatabaseManager.updateSongInDatabase(songEntity);
            Log.i("TAG", "You short clicked " + songEntity.getFirstTitle() + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SongActivity.class, new Pair[]{
                    new Pair<>(Constants.SONG_ENTITY, songEntity),
                    new Pair<>(Constants.CLICK_TYPE, Constants.ClickType.shortClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        try {
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase("*", SONG_ID, String.valueOf(songListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i("TAG", "You long clicked " + songEntityList.get(0).getFirstTitle() + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SongActivity.class, new Pair[]{
                    new Pair<>(Constants.SONG_ENTITY, songEntityList.get(0)),
                    new Pair<>(Constants.CLICK_TYPE, Constants.ClickType.longClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.song_sort_spinner) {
            Log.i("PopupMenu", "You clicked " + sortSpinner.getSelectedItem().toString());
            demandNewSearch(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}