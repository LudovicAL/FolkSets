package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.BITMAP_LIST;
import static com.bandito.folksets.util.Constants.CLICK_TYPE;
import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.DELIMITER_INPUT_PATTERN;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.POSITION;
import static com.bandito.folksets.util.Constants.SET_ENTITY;
import static com.bandito.folksets.util.Constants.TUNE_ENTITY;
import static com.bandito.folksets.util.Constants.UNIQUE_VALUES;

import static java.util.Objects.isNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
public class TuneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TuneActivity.class.getName();
    private final TuneActivity.MyBroadcastReceiver myBroadcastReceiver = new TuneActivity.MyBroadcastReceiver();
    private ProgressBar progressBar;
    private TextView progressBarHint;
    private Constants.TuneOrSet tuneOrSet;
    private int position;
    private SetEntity setEntity;
    private TuneEntity tuneEntity;
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView tuneTitlesAutoCompleteTextView;
    private AutoCompleteTextView tuneTagsAutoCompleteTextView;
    private ChipGroup tuneTagsChipGroup;
    private AutoCompleteTextView tuneComposerAutoCompleteTextView;
    private AutoCompleteTextView tuneRegionOfOriginAutoCompleteTextView;
    private AutoCompleteTextView tuneKeyAutoCompleteTextView;
    private AutoCompleteTextView tuneIncipitAutoCompleteTextView;
    private AutoCompleteTextView tuneFormAutoCompleteTextView;
    private AutoCompleteTextView tunePlayedByAutoCompleteTextView;
    private AutoCompleteTextView tuneNoteAutoCompleteTextView;

    private final TextWatcher tagTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (DELIMITER_INPUT_PATTERN.matcher(charSequence).find()) {
                String sanitizedString = charSequence.toString().replace(DEFAULT_SEPARATOR, "");
                if (!sanitizedString.isEmpty()) {
                    addChipToChipGroup(new String[]{sanitizedString}, tuneTagsChipGroup);
                }
                tuneTagsAutoCompleteTextView.setText("");
            }
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tune);

        progressBar = findViewById(R.id.tune_activity_progressBar);
        progressBarHint = findViewById(R.id.tune_activity_progressBarHintTextView);

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

        View headerView = ((NavigationView)findViewById(R.id.tune_nav_view)).getHeaderView(0);
        //Set listeners
        headerView.findViewById(R.id.back_from_edit_tune_fab).setOnClickListener(this);
        headerView.findViewById(R.id.button_save_tune).setOnClickListener(this);
        findViewById(R.id.edit_tune_fab).setOnClickListener(this);
        findViewById(R.id.back_tune_fab).setOnClickListener(this);

        tuneTagsChipGroup = headerView.findViewById(R.id.nav_tune_tags_chipGroup);
        //Prepare the autocompletes
        tuneTitlesAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_title_textview);
        tuneTitlesAutoCompleteTextView.setThreshold(0);
        tuneTagsAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_tags_textview);
        tuneTagsAutoCompleteTextView.setThreshold(0);
        tuneTagsAutoCompleteTextView.addTextChangedListener(tagTextWatcher);
        tuneComposerAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_composer_textview);
        tuneComposerAutoCompleteTextView.setThreshold(0);
        tuneRegionOfOriginAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_region_textview);
        tuneRegionOfOriginAutoCompleteTextView.setThreshold(0);
        tuneKeyAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_key_textview);
        tuneKeyAutoCompleteTextView.setThreshold(0);
        tuneIncipitAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_incipit_textview);
        tuneIncipitAutoCompleteTextView.setThreshold(0);
        tuneFormAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_form_textview);
        tuneFormAutoCompleteTextView.setThreshold(0);
        tunePlayedByAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_players_textview);
        tunePlayedByAutoCompleteTextView.setThreshold(0);
        tuneNoteAutoCompleteTextView = headerView.findViewById(R.id.nav_tune_note_textview);
        tuneNoteAutoCompleteTextView.setThreshold(0);
        prepareAutocompleteAdapters();

        //Display the data
        try {
            tuneTitlesAutoCompleteTextView.setText(tuneEntity.tuneTitles);
            addChipToChipGroup(StringUtils.split(tuneEntity.tuneTags, DEFAULT_SEPARATOR), tuneTagsChipGroup);
            tuneComposerAutoCompleteTextView.setText(tuneEntity.tuneComposer);
            tuneRegionOfOriginAutoCompleteTextView.setText(tuneEntity.tuneRegionOfOrigin);
            tuneKeyAutoCompleteTextView.setText(tuneEntity.tuneKey);
            tuneIncipitAutoCompleteTextView.setText(tuneEntity.tuneIncipit);
            tuneFormAutoCompleteTextView.setText(tuneEntity.tuneForm);
            tunePlayedByAutoCompleteTextView.setText(tuneEntity.tunePlayedBy);
            tuneNoteAutoCompleteTextView.setText(tuneEntity.tuneNote);
            ((TextView)headerView.findViewById(R.id.nav_tune_filePath_textview)).setText(tuneEntity.tuneFilePath);
            ((TextView)headerView.findViewById(R.id.nav_tune_fileType_textview)).setText(tuneEntity.tuneFileType);
            ((TextView)headerView.findViewById(R.id.nav_tune_creation_date_textview)).setText(tuneEntity.tuneFileCreationDate);
            ((TextView)headerView.findViewById(R.id.nav_tune_consultation_date_textview)).setText(tuneEntity.tuneLastConsultationDate);
            ((TextView)headerView.findViewById(R.id.nav_tune_consultation_number_textview)).setText(tuneEntity.tuneConsultationNumber.toString());
        } catch (Exception e) {
            ExceptionManager.manageException(this, e);
        }

        drawerLayout = findViewById(R.id.drawer_layout_tune);
        String clickType = getIntent().getExtras().getString(CLICK_TYPE);
        if (Constants.ClickType.longClick.toString().equals(clickType)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }

        try {
            ServiceSingleton.getInstance().renderPdf(this, tuneEntity);
        } catch (FolkSetsException e) {
            ExceptionManager.manageException(this, e);
        }
    }

    private void addChipToChipGroup(String[] chipContentArray, ChipGroup chipGroup) {
        if (isNull(chipContentArray)) {
            return;
        }
        for (String chipContent : chipContentArray) {
            Chip chip = new Chip(this);
            chip.setText(chipContent);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chipGroup.addView(chip);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chipGroup.removeView(view);
                }
            });
        }
    }

    private String retrieveChipsFromChipGroup(ChipGroup chipGroup) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, max = chipGroup.getChildCount(); i < max; i++) {
            stringBuilder.append(((Chip)chipGroup.getChildAt(i)).getText());
            if (i < max - 1) {
                stringBuilder.append(DEFAULT_SEPARATOR);
            }
        }
        return stringBuilder.toString();
    }

    private void saveTune() {
        tuneEntity.tuneTitles = tuneTitlesAutoCompleteTextView.getText().toString();
        if (tuneEntity.tuneTitles.isEmpty()) {
            Toast.makeText(this, "A title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        tuneEntity.tuneTags = retrieveChipsFromChipGroup(tuneTagsChipGroup);
        tuneEntity.tuneComposer = tuneComposerAutoCompleteTextView.getText().toString();
        tuneEntity.tuneRegionOfOrigin = tuneRegionOfOriginAutoCompleteTextView.getText().toString();
        tuneEntity.tuneKey = tuneKeyAutoCompleteTextView.getText().toString();
        tuneEntity.tuneIncipit = tuneIncipitAutoCompleteTextView.getText().toString();
        tuneEntity.tuneForm = tuneFormAutoCompleteTextView.getText().toString();
        tuneEntity.tunePlayedBy = tunePlayedByAutoCompleteTextView.getText().toString();
        tuneEntity.tuneNote = tuneNoteAutoCompleteTextView.getText().toString();
        try {
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            Toast.makeText(this, "Tune saved", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.END);
        } catch (Exception e) {
            ExceptionManager.manageException(this, e);
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
            ExceptionManager.manageException(this, e);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.tuneActivityProgressUpdate.toString()));
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.staticDataUpdate.toString()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.edit_tune_fab) {
            drawerLayout.openDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.back_from_edit_tune_fab) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.button_save_tune) {
            saveTune();
        } else if (view.getId() == R.id.back_tune_fab) {
            this.finish();
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
        tuneTagsAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneTagArray));
        tuneComposerAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneComposerArray));
        tuneRegionOfOriginAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneRegionArray));
        tuneKeyAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneKeyArray));
        tuneIncipitAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneIncipitArray));
        tuneFormAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneFormArray));
        tunePlayedByAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTunePlayedByArray));
        tuneNoteAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneNoteArray));
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
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
                }
            }
        }
    }

    private void retrieveBitmaps() {
        TunePagesRecyclerViewAdapter tunePagesRecyclerViewAdapter = new TunePagesRecyclerViewAdapter(StaticData.bitmapList);
        RecyclerView recyclerView = findViewById(R.id.tunePagesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(tunePagesRecyclerViewAdapter);
        tunePagesRecyclerViewAdapter.notifyDataSetChanged();
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
    }
}