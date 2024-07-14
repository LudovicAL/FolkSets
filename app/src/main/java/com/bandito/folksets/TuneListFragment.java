package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import android.widget.Spinner;

import com.bandito.folksets.adapters.TuneListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TuneListFragment extends Fragment implements AdapterView.OnItemSelectedListener, TuneListRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = TuneListFragment.class.getName();
    private final MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    private Spinner sortSpinner;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private final MaterialButtonToggleGroup.OnButtonCheckedListener materialButtonToggleGroupCheckedListener = (group, checkedId, isChecked) -> {
        if (isChecked) {
            updateSearchBarHint();
            demandNewSearch(false);
        }
    };
    private Timer timer;
    private TextInputEditText textInputEditText;
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
    public TuneListRecyclerViewAdapter tuneListRecyclerViewAdapter;

    public TuneListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tune_list, container, false);
        materialButtonToggleGroup = view.findViewById(R.id.fragment_tune_list_materialbuttontogglegroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(materialButtonToggleGroupCheckedListener);
        textInputEditText = view.findViewById(R.id.fragment_tune_list_textinputedittext);
        textInputEditText.addTextChangedListener(textWatcher);
        RecyclerView recyclerView = view.findViewById(R.id.fragment_tune_list_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        tuneListRecyclerViewAdapter = new TuneListRecyclerViewAdapter();
        tuneListRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(tuneListRecyclerViewAdapter);
        sortSpinner = view.findViewById(R.id.fragment_tune_list_spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tune_sort_array,
                R.layout.spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(0,false);
        sortSpinner.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myBroadcastReceiver, new IntentFilter(BroadcastName.staticDataUpdate.toString()));
        updateSearchBarHint();
        demandNewSearch(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(myBroadcastReceiver);
    }

    private void demandNewSearch(boolean userIsTyping) {
        if (timer != null) {
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
            return new Pair<>(TUNE_TITLES, SORT_ASC);
        } else if (getString(R.string.sort_by_last_created).equals(sortParameterStr)) {
            return new Pair<>(TUNE_FILE_CREATION_DATE, SORT_DESC);
        } else if (getString(R.string.sort_by_last_consulted).equals(sortParameterStr)) {
            return new Pair<>(TUNE_LAST_CONSULTATION_DATE, SORT_DESC);
        } else if (getString(R.string.sort_by_most_consulted).equals(sortParameterStr)) {
            return new Pair<>(TUNE_CONSULTATION_NUMBER, SORT_DESC);
        } else {
            throw new FolkSetsException("An unrecognized sort parameter was encountered.", null);
        }
    }

    private void performSearch() {
        try {
            DatabaseManager.initializeDatabase(requireContext());
            String textToSearch = textInputEditText.getText().toString();
            Pair<String, String> sortParameters = getSortParameters();
            if (textToSearch.isEmpty()) {
                tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, null, null, sortParameters.first, sortParameters.second));
            } else {
                int i = materialButtonToggleGroup.getCheckedButtonId();
                if (i == R.id.fragment_tune_title_materialbutton) {
                    Log.i(TAG, "Seaching title: " + textToSearch);
                    String[] titleArray = StringUtils.split(textToSearch, DEFAULT_SEPARATOR);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_TITLES, titleArray, sortParameters.first, sortParameters.second));
                } else if (i == R.id.fragment_tune_tag_materialbutton) {
                    Log.i(TAG, "Seaching tag: " + textToSearch);
                    String[] tagArray = StringUtils.split(textToSearch, DEFAULT_SEPARATOR);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_TAGS, tagArray, sortParameters.first, sortParameters.second));
                } else if (i == R.id.fragment_tune_composer_materialbutton) {
                    Log.i(TAG, "Seaching composer: " + textToSearch);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_COMPOSER, new String[]{textToSearch}, sortParameters.first, sortParameters.second));
                } else if (i == R.id.fragment_tune_region_materialbutton) {
                    Log.i(TAG, "Seaching region: " + textToSearch);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_REGION_OF_ORIGIN, new String[]{textToSearch}, sortParameters.first, sortParameters.second));
                } else if (i == R.id.fragment_tune_key_materialbutton) {
                    Log.i(TAG, "Seaching key: " + textToSearch);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_KEY, new String[]{textToSearch}, sortParameters.first, sortParameters.second));
                } else if (i == R.id.fragment_tune_playedby_materialbutton) {
                    Log.i(TAG, "Seaching played by: " + textToSearch);
                    String[] playedByArray = StringUtils.split(textToSearch, DEFAULT_SEPARATOR);
                    tuneListRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, TUNE_PLAYED_BY, playedByArray, sortParameters.first, sortParameters.second));
                }
            }
            tuneListRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, e);
        }
    }

    private void updateSearchBarHint() {
        try {
            String searchMethod = ((MaterialButton) requireActivity().findViewById(materialButtonToggleGroup.getCheckedButtonId())).getText().toString().toLowerCase();
            searchMethod = getResources().getString(R.string.played_by).equalsIgnoreCase(searchMethod) ? "player" : searchMethod;
            String sortMethod = sortSpinner.getSelectedItem().toString().toLowerCase();
            String hint = "Search by " + searchMethod + ", " + sortMethod;
            ((TextInputLayout) getActivity().findViewById((R.id.fragment_tune_list_textinputlayout))).setHint(hint);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, e);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            TuneEntity tuneEntity = DatabaseManager.findTuneByIdInDatabase("*", String.valueOf(tuneListRecyclerViewAdapter.getItem(position)), null, null).get(0);
            tuneEntity.tuneConsultationNumber++;
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            Log.i(TAG, "You short clicked " + tuneEntity.getFirstTitle() + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), TuneActivity.class, new Pair[]{
                    new Pair<>(OPERATION, TUNE_ENTITY),
                    new Pair<>(TUNE_ENTITY, tuneEntity),
                    new Pair<>(CLICK_TYPE, Constants.ClickType.shortClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, e);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        try {
            List<TuneEntity> tuneEntityList = DatabaseManager.findTuneByIdInDatabase("*", String.valueOf(tuneListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i(TAG, "You long clicked " + tuneEntityList.get(0).getFirstTitle() + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), TuneActivity.class, new Pair[]{
                    new Pair<>(OPERATION, TUNE_ENTITY),
                    new Pair<>(TUNE_ENTITY, tuneEntityList.get(0)),
                    new Pair<>(CLICK_TYPE, Constants.ClickType.longClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.fragment_tune_list_spinner) {
            Log.i(TAG, "You clicked " + sortSpinner.getSelectedItem().toString());
            updateSearchBarHint();
            demandNewSearch(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TUNE_ENTITY_LIST.equals(intent.getExtras().getString(BroadcastKey.staticDataValue.toString()))) {
                tuneListRecyclerViewAdapter.setTuneEntityList(StaticData.tuneEntityList);
                tuneListRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }
}