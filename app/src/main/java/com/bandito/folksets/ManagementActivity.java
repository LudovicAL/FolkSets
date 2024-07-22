package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.TUNE_TITLES;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.adapters.StringArrayArrayAdapter;
import com.bandito.folksets.adapters.TuneSelectorRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ManagementActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TuneSelectorRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = ManagementActivity.class.getName();
    private Activity activity;
    private Context context;
    private Constants.ManagementOperation tagsOrPlayers;
    private Dialog dialog;
    private TextView searchTextView;
    private TextInputEditText searchTextInputEditText;
    private Timer timer;
    private TuneSelectorRecyclerViewAdapter tuneSelectorRecyclerViewAdapter;
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
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an AfterTextChanged event.", e));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_management_constraintlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            activity = this;
            context = this;
            tagsOrPlayers = (Constants.ManagementOperation)getIntent().getExtras().getSerializable(OPERATION);
            searchTextView = findViewById(R.id.activity_management_search_textview);
            searchTextInputEditText = findViewById(R.id.activity_management_textinputedittext);
            RecyclerView recyclerView = findViewById(R.id.activity_management_tunelist_recyclerview);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            tuneSelectorRecyclerViewAdapter = new TuneSelectorRecyclerViewAdapter(this, this, tagsOrPlayers);
            tuneSelectorRecyclerViewAdapter.setClickListener(this);
            recyclerView.setAdapter(tuneSelectorRecyclerViewAdapter);
            ((TextView)findViewById(R.id.activity_management_header_textview)).setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.manage_tags : R.string.manage_players);
            ((Button)findViewById(R.id.activity_management_createnew_button)).setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.create_new_tag : R.string.create_new_player);
            searchTextView.setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.select_a_tag : R.string.select_a_player);
            searchTextInputEditText.addTextChangedListener(textWatcher);
            if (tagsOrPlayers == Constants.ManagementOperation.manageTags) {
                StaticData.uniqueTuneTagArray = DatabaseManager.getAllUniqueTagInTuneTable();
            } else {
                StaticData.uniqueTunePlayedByArray = DatabaseManager.getAllUniquePlayedByInTuneTable();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured during the OnCreate step of class ManagementActivity.", e, true));
        }
    }

    public void onBackButtonClick(View view) {
        try {
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnBackButtonClick event.", e));
        }
    }

    public void onCreateNewButtonClick(View view) {
        try {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.stringinput_dialog);
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((TextView)dialog.findViewById(R.id.stringinput_dialog_textview)).setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.create_new_tag : R.string.create_new_player);
            dialog.show();
            dialog.findViewById(R.id.stringinput_dialog_back_floatingActionButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.stringinput_dialog_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String value = ((EditText) dialog.findViewById(R.id.stringinput_dialog_edittext)).getText().toString();
                        if (StringUtils.isEmpty(value)) {
                            Toast.makeText(context, "Enter at least one character.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (value.contains(DEFAULT_SEPARATOR)) {
                            Toast.makeText(context, "The character ';' is forbidden.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (tagsOrPlayers == Constants.ManagementOperation.manageTags) {
                            StaticData.uniqueTuneTagArray = ArrayUtils.add(StaticData.uniqueTuneTagArray, value);
                        } else {
                            StaticData.uniqueTunePlayedByArray = ArrayUtils.add(StaticData.uniqueTunePlayedByArray, value);
                        }
                        selectString(value);
                        dialog.dismiss();
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
                    }
                }
            });
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnCreateNewButtonClick event.", e));
        }
    }

    public void openSelectDialog(View view) {
        try {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.searchable_spinner);
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((TextView)dialog.findViewById(R.id.searchable_spinner_textview)).setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.select_a_tag : R.string.select_a_player);
            ((EditText)dialog.findViewById(R.id.searchable_spinner_edittext)).setHint(tagsOrPlayers == Constants.ManagementOperation.manageTags ? R.string.search_tags : R.string.search_players);
            dialog.show();
            dialog.findViewById(R.id.searchable_spinner_back_floatingActionButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            ListView listView = dialog.findViewById(R.id.searchable_spinner_listview);
            StringArrayArrayAdapter arrayAdapter = new StringArrayArrayAdapter(this, android.R.layout.simple_list_item_1, tagsOrPlayers == Constants.ManagementOperation.manageTags ? StaticData.uniqueTuneTagArray : StaticData.uniqueTunePlayedByArray);
            listView.setAdapter(arrayAdapter);
            ((EditText)dialog.findViewById(R.id.searchable_spinner_edittext)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    try {
                        arrayAdapter.getFilter().filter(charSequence);
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an OnTextChanged event.", e));
                    }
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        selectString(arrayAdapter.getItem(position));
                        dialog.dismiss();
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured wile processing an OnItemClick event.", e));
                    }
                }
            });
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OpenSelectDialog event.", e));
        }
    }

    private void selectString(String str) {
        try {
            searchTextView.setText(str);
            tuneSelectorRecyclerViewAdapter.setCurrentTagOrPlayer(str);
            demandNewSearch(false);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while selecting a tune.", e));
        }
    }

    //The following strange bit of code make it so EditText loose the focus when we touch outside them.
    //It applies even in fragments that are "children" of this activity.
    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            Utilities.dispatchTouchEvent(motionEvent, getCurrentFocus(), (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            return super.dispatchTouchEvent(motionEvent);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing dispatchTouchEvent.", e));
            return true;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            demandNewSearch(false);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnItemSelected event.", e));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
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
                            activity.runOnUiThread(() -> {
                                try {
                                    performSearch();
                                } catch (Exception e) {
                                    ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while performing a search on the UI thread.", e));
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
        DatabaseManager.initializeDatabase(this);
        String textToSearch = searchTextInputEditText.getText().toString();
        if (StringUtils.isEmpty(searchTextView.getText())) {
            tuneSelectorRecyclerViewAdapter.setTuneEntityList(new ArrayList<>());
        } else {
            if (StringUtils.isEmpty(textToSearch)) {
                tuneSelectorRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, TUNE_TITLES, null));
            } else {
                tuneSelectorRecyclerViewAdapter.setTuneEntityList(DatabaseManager.findTunesWithValueInListInDatabase("*", TUNE_TITLES, new String[]{textToSearch}, TUNE_TITLES, null));
            }
        }
        tuneSelectorRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(TuneSelectorRecyclerViewAdapter.TuneViewHolder tuneViewHolder, View view, int position) {
        try {
            TuneEntity tuneEntity = tuneSelectorRecyclerViewAdapter.getTuneEntityList().get(position);
            if (tagsOrPlayers == Constants.ManagementOperation.manageTags) {
                if (tuneEntity.hasTag(searchTextView.getText().toString())) {
                    tuneEntity.removeTag(searchTextView.getText().toString());
                    tuneViewHolder.markAsSelected(false);
                } else {
                    tuneEntity.addTag(searchTextView.getText().toString());
                    tuneViewHolder.markAsSelected(true);
                }
            } else {
                if (tuneEntity.hasPlayer(searchTextView.getText().toString())) {
                    tuneEntity.removePlayer(searchTextView.getText().toString());
                    tuneViewHolder.markAsSelected(false);
                } else {
                    tuneEntity.addPlayer(searchTextView.getText().toString());
                    tuneViewHolder.markAsSelected(true);
                }
            }
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            tuneSelectorRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnItemClick event.", e));
        }
    }

    @Override
    public void onLongItemClick(TuneSelectorRecyclerViewAdapter.TuneViewHolder tuneViewHolder, View view, int position) {
        try {
            onItemClick(tuneViewHolder, view, position);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnLongItemClick event.", e));
        }
    }
}