package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.CLICK_TYPE;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.POSITION;
import static com.bandito.folksets.util.Constants.SET_ENTITY;
import static com.bandito.folksets.util.Constants.SET_ENTITY_LIST;
import static com.bandito.folksets.util.Constants.SET_NAME;

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
import android.widget.TextView;

import com.bandito.folksets.adapters.SetListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SetListFragment extends Fragment implements View.OnClickListener, SetListRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = SetListFragment.class.getName();
    private final SetListFragment.MyBroadcastReceiver myBroadcastReceiver = new SetListFragment.MyBroadcastReceiver();
    private TextView setMatchNumberTextview;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private final MaterialButtonToggleGroup.OnButtonCheckedListener materialButtonToggleGroupCheckedListener = (group, checkedId, isChecked) -> {
        try {
            if (isChecked) {
                if (setMatchNumberTextview != null) {
                    if (checkedId == R.id.fragment_set_list_tuneinsets_materialbutton) {
                        setMatchNumberTextview.setVisibility(View.VISIBLE);
                    } else {
                        setMatchNumberTextview.setVisibility(View.GONE);
                    }
                }
                updateSearchBarHint();
                demandNewSearch(false);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing a toggle button checked event.", e));
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
            try {
                demandNewSearch(true);
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing and AfterTextChanged event.", e));
            }
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
        try {
            materialButtonToggleGroup = view.findViewById(R.id.fragment_set_list_materialbuttontogglegroup);
            materialButtonToggleGroup.addOnButtonCheckedListener(materialButtonToggleGroupCheckedListener);
            textInputEditText = view.findViewById(R.id.fragment_set_list_textinputedittext);
            textInputEditText.addTextChangedListener(textWatcher);
            RecyclerView recyclerView = view.findViewById(R.id.fragment_set_list_recyclerview);
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            setListRecyclerViewAdapter = new SetListRecyclerViewAdapter(requireActivity(), requireContext());
            setListRecyclerViewAdapter.setClickListener(this);
            recyclerView.setAdapter(setListRecyclerViewAdapter);
            setMatchNumberTextview = view.findViewById(R.id.fragment_set_list_matchcount_textview);
            view.findViewById(R.id.fragment_set_list_createnewset_button).setOnClickListener(this);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured during the OnCreateView step of class SetListFragment.", e, true));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.staticDataUpdate.toString()));
            updateSearchBarHint();
            demandNewSearch(false);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while resuming SetListFragment.", e, true));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while pausing SetListFragment.", e, true));
        }
    }

    private void demandNewSearch(boolean userIsTyping) throws FolkSetsException {
        if (timer != null) {
            timer.cancel();
        }
        if (userIsTyping) {
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    performSearch();
                                } catch (FolkSetsException e) {
                                    ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while performing a search on UI thread.", e));
                                }
                            });
                        }
                    },
                    500L);
        } else {
            performSearch();
        }
    }

    private void performSearch() throws FolkSetsException {
        String textToSearch = textInputEditText.getText().toString();
        if (textToSearch.isEmpty()) {
            setListRecyclerViewAdapter.setSetEntityList(DatabaseManager.findAllSetsInDatabase("*", SET_NAME, null));
        } else {
            int i = materialButtonToggleGroup.getCheckedButtonId();
            if (i == R.id.fragment_set_list_setname_materialbutton) {
                Log.i(TAG, "Seaching name: " + textToSearch);
                setListRecyclerViewAdapter.setSetEntityList(DatabaseManager.findSetsByNameInDatabase("*", textToSearch, SET_NAME, null));
            } else if (i == R.id.fragment_set_list_tuneinsets_materialbutton) {
                Log.i(TAG, "Seaching tune in set: " + textToSearch);
                Pair<Integer, List<SetEntity>> result = DatabaseManager.findSetsWithTunesInDatabase(textToSearch, SET_NAME, null);
                setMatchNumber(result.first);
                setListRecyclerViewAdapter.setSetEntityList(result.second);
            }
        }
        setListRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void updateSearchBarHint() {
        String searchMethod = ((MaterialButton)getActivity().findViewById(materialButtonToggleGroup.getCheckedButtonId())).getText().toString().toLowerCase();
        String hint = "Search by " + searchMethod;
        ((TextInputLayout)getActivity().findViewById((R.id.fragment_set_list_textinputlayout))).setHint(hint);
    }

    private void setMatchNumber(int matchNumber) {
        if (matchNumber > 1) {
            setMatchNumberTextview.setText(matchNumber + " tunes matching your prompt");
        } else {
            setMatchNumberTextview.setText(matchNumber + " tune matching your prompt");
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetByIdInDatabase("*", String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i(TAG, "You short clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), TuneActivity.class, new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.set),
                    new Pair<>(POSITION, 0),
                    new Pair<>(SET_ENTITY, setEntityList.get(0)),
                    new Pair<>(CLICK_TYPE, Constants.ClickType.shortClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnItemClick event.", e));
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetByIdInDatabase("*", String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i(TAG, "You long clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SetActivity.class, new Pair[]{
                    new Pair<>(OPERATION, Constants.SetOperation.editSet),
                    new Pair<>(SET_ENTITY, setEntityList.get(0))
            });
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnItemClick event.", e));
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.fragment_set_list_createnewset_button) {
                loadSetActivity();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
        }
    }

    private void loadSetActivity() throws FolkSetsException {
        Utilities.loadActivity(requireActivity(), requireContext(), SetActivity.class, new Pair[]{
                new Pair<>(OPERATION, Constants.SetOperation.createSet)
        });
    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (SET_ENTITY_LIST.equals(intent.getExtras().getString(Constants.BroadcastKey.staticDataValue.toString()))) {
                    setListRecyclerViewAdapter.setSetEntityList(StaticData.setEntityList);
                    setListRecyclerViewAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnReceive event.", e));
            }
        }
    }
}