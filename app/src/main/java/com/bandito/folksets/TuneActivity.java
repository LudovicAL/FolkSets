package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.adapters.TunePagesRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.services.ServiceSingleton;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.ChipGroupUtilities;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;
public class TuneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TuneActivity.class.getName();
    private final Activity activity = this;
    private final TuneActivity.MyBroadcastReceiver myBroadcastReceiver = new TuneActivity.MyBroadcastReceiver();
    private ProgressBar progressBar;
    private TextView progressBarHint;
    private Constants.TuneOrSet tuneOrSet;
    private int position;
    private SetEntity setEntity;
    private TuneEntity tuneEntity;
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView tuneTitlesAutoCompleteTextView;
    private ChipGroup tuneTitlesChipGroup;
    private AutoCompleteTextView tuneTagsAutoCompleteTextView;
    private ChipGroup tuneTagsChipGroup;
    private AutoCompleteTextView tuneComposerAutoCompleteTextView;
    private AutoCompleteTextView tuneRegionOfOriginAutoCompleteTextView;
    private AutoCompleteTextView tuneKeyAutoCompleteTextView;
    private AutoCompleteTextView tuneIncipitAutoCompleteTextView;
    private AutoCompleteTextView tuneFormAutoCompleteTextView;
    private AutoCompleteTextView tunePlayedByAutoCompleteTextView;
    private ChipGroup tunePlayedByChipGroup;
    private AutoCompleteTextView tuneNoteAutoCompleteTextView;
    private TextWatcher titleTextWatcher;
    private TextWatcher tagTextWatcher;
    private TextWatcher playedByTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tune);

        //Find views
        View headerView = ((NavigationView)findViewById(R.id.activity_tune_navigationview)).getHeaderView(0);
        progressBar = findViewById(R.id.activity_tune_progressbar);
        progressBarHint = findViewById(R.id.activity_tune_progressbarhint_textview);
        tuneTitlesChipGroup = headerView.findViewById(R.id.tune_nav_header_title_chipgroup);
        tuneTagsChipGroup = headerView.findViewById(R.id.tune_nav_header_tag_chipgroup);
        tunePlayedByChipGroup = headerView.findViewById(R.id.tune_nav_header_players_chipgroup);
        tuneTitlesAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_title_autocompletetextview);
        tuneTagsAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_tag_autocompletetextview);
        tuneComposerAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_composer_autocompletetextview);
        tuneRegionOfOriginAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_region_autocompletetextview);
        tuneKeyAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_key_autocompletetextview);
        tuneIncipitAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_incipit_autocompletetextview);
        tuneFormAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_form_autocompletetextview);
        tunePlayedByAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_players_autocompletetextview);
        tuneNoteAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_note_autocompletetextview);

        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars());// | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        getWindow().setNavigationBarContrastEnforced(false);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        int navigationBarHeight = getWindowManager().getCurrentWindowMetrics().getWindowInsets().getInsets(WindowInsets.Type.navigationBars()).bottom;
        Button previousTuneButton = findViewById(R.id.activity_tune_previous_button);
        previousTuneButton.setPadding(previousTuneButton.getPaddingLeft(), previousTuneButton.getPaddingTop(), previousTuneButton.getPaddingRight(), navigationBarHeight);
        Button nextTuneButton = findViewById(R.id.activity_tune_next_button);
        nextTuneButton.setPadding(nextTuneButton.getPaddingLeft(), nextTuneButton.getPaddingTop(), nextTuneButton.getPaddingRight(), navigationBarHeight);

        //Determine what was received: a tune or a set
        String tuneOrSetStr = getIntent().getExtras().getString(OPERATION);
        tuneOrSet = Constants.TuneOrSet.set.toString().equals(tuneOrSetStr) ? Constants.TuneOrSet.set : Constants.TuneOrSet.tune;

        //Retrieve Tune data
        if (tuneOrSet.equals(Constants.TuneOrSet.tune)) {
            tuneEntity = (TuneEntity) getIntent().getExtras().getSerializable(TUNE_ENTITY);
        } else {
            setEntity = (SetEntity) getIntent().getExtras().getSerializable(SET_ENTITY);
            position = getIntent().getExtras().getInt(POSITION);
            try {
                tuneEntity = DatabaseManager.findTuneByIdInDatabase("*", setEntity.getTune(position), null, null).get(0);
            } catch (Exception e) {
                Log.e(TAG, "An error occured while fetching a tune at position " + position + " in set.", e);
            }
        }

        //Set listeners
        headerView.findViewById(R.id.tune_nav_header_back_floatingactionbutton).setOnClickListener(this);
        headerView.findViewById(R.id.tune_nav_header_save_button).setOnClickListener(this);
        findViewById(R.id.activity_tune_edit_floatingactionbutton).setOnClickListener(this);
        findViewById(R.id.activity_tune_back_floatingactionbutton).setOnClickListener(this);

        titleTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup);
        tagTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup);
        playedByTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup);
        //Prepare the autocompletes
        tuneTitlesAutoCompleteTextView.setThreshold(0);
        tuneTitlesAutoCompleteTextView.addTextChangedListener(titleTextWatcher);
        tuneTagsAutoCompleteTextView.setThreshold(0);
        tuneTagsAutoCompleteTextView.addTextChangedListener(tagTextWatcher);
        tuneComposerAutoCompleteTextView.setThreshold(0);
        tuneRegionOfOriginAutoCompleteTextView.setThreshold(0);
        tuneKeyAutoCompleteTextView.setThreshold(0);
        tuneIncipitAutoCompleteTextView.setThreshold(0);
        tuneFormAutoCompleteTextView.setThreshold(0);
        tunePlayedByAutoCompleteTextView.setThreshold(0);
        tunePlayedByAutoCompleteTextView.addTextChangedListener(playedByTextWatcher);
        tuneNoteAutoCompleteTextView.setThreshold(0);
        prepareAutocompleteAdapters();

        //Display the data
        try {
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTitles, DEFAULT_SEPARATOR), tuneTitlesChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTags, DEFAULT_SEPARATOR), tuneTagsChipGroup);
            tuneComposerAutoCompleteTextView.setText(tuneEntity.tuneComposer);
            tuneRegionOfOriginAutoCompleteTextView.setText(tuneEntity.tuneRegionOfOrigin);
            tuneKeyAutoCompleteTextView.setText(tuneEntity.tuneKey);
            tuneIncipitAutoCompleteTextView.setText(tuneEntity.tuneIncipit);
            tuneFormAutoCompleteTextView.setText(tuneEntity.tuneForm);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tunePlayedBy, DEFAULT_SEPARATOR), tunePlayedByChipGroup);
            tuneNoteAutoCompleteTextView.setText(tuneEntity.tuneNote);
            ((TextView)headerView.findViewById(R.id.tune_nav_header_filepath_autocompletetextview)).setText(tuneEntity.tuneFilePath);
            ((TextView)headerView.findViewById(R.id.tune_nav_header_filetype_autocompletetextview)).setText(tuneEntity.tuneFileType);
            ((TextView)headerView.findViewById(R.id.tune_nav_header_creationdate_autocompletetextview)).setText(tuneEntity.tuneFileCreationDate);
            ((TextView)headerView.findViewById(R.id.tune_nav_header_consultationdate_autocompletetextview)).setText(tuneEntity.tuneLastConsultationDate);
            ((TextView)headerView.findViewById(R.id.tune_nav_header_consultationnumber_autocompletetextview)).setText(tuneEntity.tuneConsultationNumber.toString());
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, e);
        }

        drawerLayout = findViewById(R.id.activity_tune_drawerlayout);
        String clickType = getIntent().getExtras().getString(CLICK_TYPE);
        if (Constants.ClickType.longClick.toString().equals(clickType)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void saveTune() {
        if (tuneTitlesChipGroup.getChildCount() == 0) {
            Toast.makeText(this, "At least one title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        tuneEntity.tuneTitles = ChipGroupUtilities.retrieveChipsFromChipGroup(tuneTitlesChipGroup);
        tuneEntity.tuneTags = ChipGroupUtilities.retrieveChipsFromChipGroup(tuneTagsChipGroup);
        tuneEntity.tuneComposer = tuneComposerAutoCompleteTextView.getText().toString();
        tuneEntity.tuneRegionOfOrigin = tuneRegionOfOriginAutoCompleteTextView.getText().toString();
        tuneEntity.tuneKey = tuneKeyAutoCompleteTextView.getText().toString();
        tuneEntity.tuneIncipit = tuneIncipitAutoCompleteTextView.getText().toString();
        tuneEntity.tuneForm = tuneFormAutoCompleteTextView.getText().toString();
        tuneEntity.tunePlayedBy = ChipGroupUtilities.retrieveChipsFromChipGroup(tunePlayedByChipGroup);
        tuneEntity.tuneNote = tuneNoteAutoCompleteTextView.getText().toString();
        try {
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            Toast.makeText(this, "Tune saved", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.END);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            tuneEntity.tuneLastConsultationDate = OffsetDateTime.now().toString();
            DatabaseManager.updateTuneInDatabase(tuneEntity);
        } catch (Exception e) {
            Log.e(TAG, "And error occured when trying to update the tune last consultation date.", e);
        }
        try {
            ServiceSingleton.getInstance().interruptPdfRendering();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, e);
        }
        StaticData.bitmapList = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.tuneActivityProgressUpdate.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.staticDataUpdate.toString()));
        try {
            ServiceSingleton.getInstance().renderPdfAndGetPreviousAndNextTune(this, this, tuneEntity, setEntity, position, tuneOrSet);
        } catch (FolkSetsException e) {
            ExceptionManager.manageException(this, this, TAG, e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.activity_tune_edit_floatingactionbutton) {
            drawerLayout.openDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.activity_tune_back_floatingactionbutton) {
            this.finish();
        } else if (view.getId() == R.id.tune_nav_header_save_button) {
            saveTune();
        } else if (view.getId() == R.id.tune_nav_header_back_floatingactionbutton) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.activity_tune_previous_button) {
            loadPreviousTune();
        } else if (view.getId() == R.id.activity_tune_next_button) {
            loadNextTune();
        }
    }

    private void loadPreviousTune() {
        Pair<String, ? extends Serializable>[] messages;
        if (tuneOrSet == Constants.TuneOrSet.set) {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.set.toString()),
                    new Pair<>(POSITION, position - 1),
                    new Pair<>(SET_ENTITY, setEntity)
            };
        } else {
            messages = new Pair[]{
                    new Pair<>(OPERATION, TUNE_ENTITY),
                    new Pair<>(TUNE_ENTITY, StaticData.previousTune)
            };
        }
        loadTuneActivity(messages);
    }

    private void loadNextTune() {
        Pair<String, ? extends Serializable>[] messages;
        if (tuneOrSet == Constants.TuneOrSet.set) {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.set.toString()),
                    new Pair<>(POSITION, position + 1),
                    new Pair<>(SET_ENTITY, setEntity)
            };
        } else {
            messages = new Pair[]{
                    new Pair<>(OPERATION, TUNE_ENTITY),
                    new Pair<>(TUNE_ENTITY, StaticData.nextTune)
            };
        }
        loadTuneActivity(messages);
    }

    private void loadTuneActivity(Pair<String, ? extends Serializable>[] messages) {
        try {
            Utilities.loadActivity(this, this, TuneActivity.class, messages);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, e);
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

    private void prepareAutocompleteAdapters() {
        tuneTitlesAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneTitleArray));
        tuneTitlesAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup));
        tuneTagsAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneTagArray));
        tuneTagsAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup));
        tuneComposerAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneComposerArray));
        tuneRegionOfOriginAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneRegionArray));
        tuneKeyAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneKeyArray));
        tuneIncipitAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneIncipitArray));
        tuneFormAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneFormArray));
        tunePlayedByAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTunePlayedByArray));
        tunePlayedByAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup));
        tuneNoteAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneNoteArray));
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("The activity receive à broadcast with not extras.", null));
            }
            if (bundle.containsKey(Constants.BroadcastKey.progressVisibility.toString())) {
                updateProgressBarVisibility(bundle.getInt(Constants.BroadcastKey.progressVisibility.toString()));
            }
            if (bundle.containsKey(Constants.BroadcastKey.progressValue.toString())) {
                updateProgressBarValue(bundle.getInt(Constants.BroadcastKey.progressValue.toString()));
            }
            if (bundle.containsKey(Constants.BroadcastKey.progressHint.toString())) {
                updateProgressBarHint(bundle.getString(Constants.BroadcastKey.progressHint.toString()));
            }
            if (bundle.containsKey(Constants.BroadcastKey.progressStepNumber.toString())) {
                updateProgressBarStepNumber(bundle.getInt(Constants.BroadcastKey.progressStepNumber.toString()));
            }
            if (bundle.containsKey(Constants.BroadcastKey.staticDataValue.toString())) {
                String broadcastValue = intent.getExtras().getString(Constants.BroadcastKey.staticDataValue.toString());
                if (BITMAP_LIST.equals(broadcastValue)) {
                    retrieveBitmaps();
                } else if (UNIQUE_VALUES.equals(broadcastValue)) {
                    prepareAutocompleteAdapters();
                } else if (PREVIOUS_AND_NEXT_TUNE.equals(broadcastValue)) {
                    displayPreviousAndNextTune();
                }
            }
        }
    }

    private void displayPreviousAndNextTune() {
        try {
            if (StaticData.previousTune != null) {
                Button previousTuneButton = findViewById(R.id.activity_tune_previous_button);
                previousTuneButton.setVisibility(View.VISIBLE);
                previousTuneButton.setText(StaticData.previousTune.getFirstTitle());
                previousTuneButton.setOnClickListener(this);
            }
            if (StaticData.nextTune != null) {
                Button nextTuneButton = findViewById(R.id.activity_tune_next_button);
                nextTuneButton.setVisibility(View.VISIBLE);
                nextTuneButton.setText(StaticData.nextTune.getFirstTitle());
                nextTuneButton.setOnClickListener(this);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, e);
        }
    }

    private void retrieveBitmaps() {
        if (StaticData.bitmapList != null) {
            TunePagesRecyclerViewAdapter tunePagesRecyclerViewAdapter = new TunePagesRecyclerViewAdapter(StaticData.bitmapList);
            RecyclerView recyclerView = findViewById(R.id.activity_tune_recyclerview);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            recyclerView.setAdapter(tunePagesRecyclerViewAdapter);
            tunePagesRecyclerViewAdapter.notifyDataSetChanged();
            StaticData.bitmapList = null;
        }
    }

    private void updateProgressBarStepNumber(int stepNumber) {
        progressBar.setMax(stepNumber);
    }

    private void updateProgressBarHint(String hint) {
        progressBarHint.setText(hint);
    }

    private void updateProgressBarValue(int progressValue) {
        progressBar.setProgress(progressValue);
    }

    private void updateProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
        progressBarHint.setVisibility(visibility);
    }
}
