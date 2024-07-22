package com.bandito.folksets;

import static android.view.Menu.NONE;
import static com.bandito.folksets.util.Constants.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
    private final Context context = this;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tune);

        try {
            //Find views
            View headerView = ((NavigationView) findViewById(R.id.activity_tune_navigationview)).getHeaderView(0);
            progressBar = findViewById(R.id.recyclerview_footer_progressbar);
            progressBarHint = findViewById(R.id.recyclerview_footer_progressbarhint_textview);
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
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            int navigationBarHeight = getWindowManager().getCurrentWindowMetrics().getWindowInsets().getInsets(WindowInsets.Type.navigationBars()).bottom;
            View previousTuneButton = findViewById(R.id.recyclerview_footer_innerbuttonprevious_constraintlayout);
            previousTuneButton.setPadding(previousTuneButton.getPaddingLeft(), previousTuneButton.getPaddingTop(), previousTuneButton.getPaddingRight(), navigationBarHeight);
            View nextTuneButton = findViewById(R.id.recyclerview_footer_innerbuttonnext_constraintlayout);
            nextTuneButton.setPadding(nextTuneButton.getPaddingLeft(), nextTuneButton.getPaddingTop(), nextTuneButton.getPaddingRight(), navigationBarHeight);

            //Determine what was received: a tune or a set
            tuneOrSet = (Constants.TuneOrSet)(getIntent().getExtras().getSerializable(OPERATION));

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

            TextWatcher titleTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup);
            TextWatcher tagTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup);
            TextWatcher playedByTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup);
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
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTitles, DEFAULT_SEPARATOR), tuneTitlesChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTags, DEFAULT_SEPARATOR), tuneTagsChipGroup);
            tuneComposerAutoCompleteTextView.setText(tuneEntity.tuneComposer);
            tuneRegionOfOriginAutoCompleteTextView.setText(tuneEntity.tuneRegionOfOrigin);
            tuneKeyAutoCompleteTextView.setText(tuneEntity.tuneKey);
            tuneIncipitAutoCompleteTextView.setText(tuneEntity.tuneIncipit);
            tuneFormAutoCompleteTextView.setText(tuneEntity.tuneForm);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tunePlayedBy, DEFAULT_SEPARATOR), tunePlayedByChipGroup);
            tuneNoteAutoCompleteTextView.setText(tuneEntity.tuneNote);
            ((TextView) headerView.findViewById(R.id.tune_nav_header_filepath_autocompletetextview)).setText(tuneEntity.tuneFilePath);
            ((TextView) headerView.findViewById(R.id.tune_nav_header_filetype_autocompletetextview)).setText(tuneEntity.tuneFileType);
            ((TextView) headerView.findViewById(R.id.tune_nav_header_creationdate_autocompletetextview)).setText(tuneEntity.tuneFileCreationDate);
            ((TextView) headerView.findViewById(R.id.tune_nav_header_consultationdate_autocompletetextview)).setText(tuneEntity.tuneLastConsultationDate);
            ((TextView) headerView.findViewById(R.id.tune_nav_header_consultationnumber_autocompletetextview)).setText(tuneEntity.tuneConsultationNumber.toString());

            drawerLayout = findViewById(R.id.activity_tune_drawerlayout);
            String clickType = getIntent().getExtras().getString(CLICK_TYPE);
            if (Constants.ClickType.longClick.toString().equals(clickType)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured during the OnCreate step of class TuneActivity.", e, true));
        }
    }

    private void saveTune() {
        try {
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
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            Toast.makeText(this, "Tune saved", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.END);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured while saving a tune data.", e));
        }
    }

    @Override
    protected void onDestroy() {
        try {
            tuneEntity.tuneLastConsultationDate = OffsetDateTime.now().toString();
            DatabaseManager.updateTuneInDatabase(tuneEntity);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured when trying to update the tune last consultation date.", e));
        }
        try {
            ServiceSingleton.getInstance().interruptPdfRendering();
            StaticData.bitmapList = null;
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured while interrupting the pdf rendering service.", e, true));
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.tuneActivityProgressUpdate.toString()));
            LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.staticDataUpdate.toString()));
            ServiceSingleton.getInstance().renderPdfAndGetPreviousAndNextTune(this, this, tuneEntity, setEntity, position, tuneOrSet);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while resuming TuneActivity.", e));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while pausing TuneActivity.", e, true));
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.activity_tune_edit_floatingactionbutton) {
                drawerLayout.openDrawer(GravityCompat.END);
            } else if (view.getId() == R.id.activity_tune_back_floatingactionbutton) {
                this.finish();
            } else if (view.getId() == R.id.tune_nav_header_save_button) {
                saveTune();
            } else if (view.getId() == R.id.tune_nav_header_back_floatingactionbutton) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else if (view.getId() == R.id.recyclerview_footer_set_textView) {
                displayPopupMenuOfSetsWithTune(view);
            } else if (view.getId() == R.id.recyclerview_footer_innerbuttonprevious_constraintlayout) {
                loadPreviousTune();
            } else if (view.getId() == R.id.recyclerview_footer_innerbuttonnext_constraintlayout) {
                loadNextTune();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
        }
    }

    private void displayPopupMenuOfSetsWithTune(View view) {
        try {
            if (tuneOrSet == TuneOrSet.set) {
                return;
            }
            if (StaticData.setsWithTune == null || StaticData.setsWithTune.isEmpty()) {
                return;
            }
            PopupMenu popupMenu = new PopupMenu(this, view);
            for (int i = 0, max = StaticData.setsWithTune.size(); i < max; i++) {
                popupMenu.getMenu().add(NONE, i, NONE, StaticData.setsWithTune.get(i).setName);
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        Utilities.loadActivity(activity, context, TuneActivity.class, new Pair[]{
                                new Pair<>(OPERATION, Constants.TuneOrSet.set),
                                new Pair<>(POSITION, 0),
                                new Pair<>(SET_ENTITY, StaticData.setsWithTune.get(menuItem.getItemId())),
                                new Pair<>(CLICK_TYPE, Constants.ClickType.shortClick.toString())
                        });
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An error occured during an OnMenuItemClick event.", e));
                    }
                    return true;
                }
            });
            popupMenu.show();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured while displaying sets with tune.", e));
        }
    }

    private void loadPreviousTune() throws FolkSetsException {
        Pair<String, ? extends Serializable>[] messages;
        if (tuneOrSet == Constants.TuneOrSet.set) {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.set),
                    new Pair<>(POSITION, position - 1),
                    new Pair<>(SET_ENTITY, setEntity)
            };
        } else {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.tune),
                    new Pair<>(TUNE_ENTITY, StaticData.previousTune)
            };
        }
        loadTuneActivity(messages);
    }

    private void loadNextTune() throws FolkSetsException {
        Pair<String, ? extends Serializable>[] messages;
        if (tuneOrSet == Constants.TuneOrSet.set) {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.set),
                    new Pair<>(POSITION, position + 1),
                    new Pair<>(SET_ENTITY, setEntity)
            };
        } else {
            messages = new Pair[]{
                    new Pair<>(OPERATION, Constants.TuneOrSet.tune),
                    new Pair<>(TUNE_ENTITY, StaticData.nextTune)
            };
        }
        loadTuneActivity(messages);
    }

    private void loadTuneActivity(Pair<String, ? extends Serializable>[] messages) throws FolkSetsException {
        Utilities.loadActivity(this, this, TuneActivity.class, messages);
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

    private void prepareAutocompleteAdapters() {
        tuneTitlesAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneTitleArray));
        tuneTitlesAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup));
        tuneTagsAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneTagArray));
        tuneTagsAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup));
        tuneComposerAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneComposerArray));
        tuneRegionOfOriginAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneRegionArray));
        tuneKeyAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneKeyArray));
        tuneIncipitAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneIncipitArray));
        tuneFormAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneFormArray));
        tunePlayedByAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTunePlayedByArray));
        tunePlayedByAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup));
        tuneNoteAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneNoteArray));
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("The activity receive à broadcast with not extras.", null));
                    return;
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
                    } else if (SETS_WITH_TUNE.equals(broadcastValue)) {
                        displaySetsWithTune();
                    }
                }
            } catch (Exception e) {
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured during an OnReceive event.", e));
            }
        }
    }

    private void displayPreviousAndNextTune() throws FolkSetsException {
        if (StaticData.previousTune != null) {
            View previousTuneButton = findViewById(R.id.recyclerview_footer_innerbuttonprevious_constraintlayout);
            previousTuneButton.setVisibility(View.VISIBLE);
            previousTuneButton.setOnClickListener(this);
            ((TextView)findViewById(R.id.recyclerview_footer_previousfooter_textview)).setText(StaticData.previousTune.getFirstTitle());
        }
        if (StaticData.nextTune != null) {
            View nextTuneButton = findViewById(R.id.recyclerview_footer_innerbuttonnext_constraintlayout);
            nextTuneButton.setVisibility(View.VISIBLE);
            nextTuneButton.setOnClickListener(this);
            ((TextView)findViewById(R.id.recyclerview_footer_nextfooter_textview)).setText(StaticData.nextTune.getFirstTitle());
        }
    }

    private void displaySetsWithTune() {
        if (tuneOrSet == TuneOrSet.set) {
            return;
        }
        if (StaticData.setsWithTune == null || StaticData.setsWithTune.isEmpty()) {
            return;
        }
        TextView setTextView = findViewById(R.id.recyclerview_footer_set_textView);
        setTextView.setVisibility(View.VISIBLE);
        setTextView.setOnClickListener(this);
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
