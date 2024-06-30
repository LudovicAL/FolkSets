package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.SET_ID;
import static com.bandito.folksets.util.Constants.SET_NAME;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bandito.folksets.adapters.SetListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SetListFragment extends Fragment implements View.OnClickListener, SetListRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = SetListFragment.class.getName();
    private ProgressBar progressBar;
    private TextView setMatchNumberTextview;

    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private final MaterialButtonToggleGroup.OnButtonCheckedListener materialButtonToggleGroupCheckedListener = (group, checkedId, isChecked) -> {
        if (isChecked) {
            if (!isNull(setMatchNumberTextview)) {
                if (checkedId == R.id.toggleButtonSongInSet) {
                    setMatchNumberTextview.setVisibility(View.VISIBLE);
                } else {
                    setMatchNumberTextview.setVisibility(View.GONE);
                }
            }
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
    public SetListRecyclerViewAdapter setListRecyclerViewAdapter;

    public SetListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_list, container, false);
        progressBar = view.findViewById(R.id.setListProgressBar);
        materialButtonToggleGroup = view.findViewById(R.id.setToggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(materialButtonToggleGroupCheckedListener);
        editText = view.findViewById(R.id.setListEditText);
        editText.addTextChangedListener(textWatcher);
        RecyclerView recyclerView = view.findViewById(R.id.setListRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        setListRecyclerViewAdapter = new SetListRecyclerViewAdapter();
        setListRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(setListRecyclerViewAdapter);
        setMatchNumberTextview = view.findViewById(R.id.set_match_number_textview);
        view.findViewById(R.id.create_new_set_button).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        demandNewSearch(false);
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

    private void performSearch() {
        try {
            String textToSearch = editText.getText().toString();
            if (textToSearch.isEmpty()) {
                SetListRecyclerViewAdapter.setEntityList = DatabaseManager.findSetsInDatabase(SET_ID + "," + SET_NAME, null, null, SET_NAME, null);
            } else {
                int i = materialButtonToggleGroup.getCheckedButtonId();
                if (i == R.id.toggleButtonSetName) {
                    Log.i(TAG, "Seaching name: " + textToSearch);
                    SetListRecyclerViewAdapter.setEntityList = DatabaseManager.findSetsInDatabase(SET_ID + "," + SET_NAME, SET_NAME, textToSearch, SET_NAME, null);
                } else if (i == R.id.toggleButtonSongInSet) {
                    Log.i(TAG, "Seaching song in set: " + textToSearch);
                    Pair<Integer, List<SetEntity>> result = DatabaseManager.findSetsWithSongsInDatabase(textToSearch, SET_NAME, null);
                    setMatchNumber(result.first);
                    SetListRecyclerViewAdapter.setEntityList = result.second;
                }
            }
            setListRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    private void setMatchNumber(int matchNumber) {
        if (matchNumber > 1) {
            setMatchNumberTextview.setText(matchNumber + " songs matching your prompt");
        } else {
            setMatchNumberTextview.setText(matchNumber + " song matching your prompt");
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", SET_ID, String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i(TAG, "You short clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SongActivity.class, new Pair[]{
                    new Pair<>(Constants.OPERATION, Constants.SET_ENTITY),
                    new Pair<>(Constants.POSITION, 0),
                    new Pair<>(Constants.SET_ENTITY, setEntityList.get(0)),
                    new Pair<>(Constants.CLICK_TYPE, Constants.ClickType.shortClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", SET_ID, String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i(TAG, "You long clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SetActivity.class, new Pair[]{
                    new Pair<>(Constants.OPERATION, Constants.SetOperation.editSet.toString()),
                    new Pair<>(Constants.SET_ENTITY, setEntityList.get(0))
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_new_set_button) {
            Utilities.loadActivity(requireActivity(), requireContext(), SetActivity.class, new Pair[]{
                    new Pair<>(Constants.OPERATION, Constants.SetOperation.createSet.toString())
            });
        }
    }
}