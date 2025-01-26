package com.bandito.folksets;

import static android.view.Menu.NONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.bandito.folksets.util.Constants.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.core.content.ContextCompat;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private float currentZoom = ZOOM_START;
    private SetEntity setEntity;
    private TuneEntity tuneEntity;
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView tuneTitlesAutoCompleteTextView;
    private ChipGroup tuneTitlesChipGroup;
    private AutoCompleteTextView tuneTagsAutoCompleteTextView;
    private ChipGroup tuneTagsChipGroup;
    private AutoCompleteTextView tuneComposersAutoCompleteTextView;
    private ChipGroup tuneComposersChipGroup;
    private AutoCompleteTextView tuneRegionsOfOriginAutoCompleteTextView;
    private ChipGroup tuneRegionsOfOriginChipGroup;
    private AutoCompleteTextView tuneKeysAutoCompleteTextView;
    private ChipGroup tuneKeysChipGroup;
    private AutoCompleteTextView tuneIncipitAutoCompleteTextView;
    private AutoCompleteTextView tuneFormAutoCompleteTextView;
    private AutoCompleteTextView tunePlayedByAutoCompleteTextView;
    private ChipGroup tunePlayedByChipGroup;
    private AutoCompleteTextView tuneNoteAutoCompleteTextView;
    private FloatingActionButton zoomInFloatingActionButton;
    private FloatingActionButton zoomOutFloatingActionButton;
    private TunePagesRecyclerViewAdapter tunePagesRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private Dialog dialog;

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
            tuneComposersChipGroup = headerView.findViewById(R.id.tune_nav_header_composer_chipgroup);
            tuneRegionsOfOriginChipGroup = headerView.findViewById(R.id.tune_nav_header_region_chipgroup);
            tuneKeysChipGroup = headerView.findViewById(R.id.tune_nav_header_key_chipgroup);
            tunePlayedByChipGroup = headerView.findViewById(R.id.tune_nav_header_players_chipgroup);
            tuneTitlesAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_title_autocompletetextview);
            tuneTagsAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_tag_autocompletetextview);
            tuneComposersAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_composer_autocompletetextview);
            tuneRegionsOfOriginAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_region_autocompletetextview);
            tuneKeysAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_key_autocompletetextview);
            tuneIncipitAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_incipit_autocompletetextview);
            tuneFormAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_form_autocompletetextview);
            tunePlayedByAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_players_autocompletetextview);
            tuneNoteAutoCompleteTextView = headerView.findViewById(R.id.tune_nav_header_note_autocompletetextview);
            zoomInFloatingActionButton = findViewById(R.id.activity_tune_zoomin_floatingactionbutton);
            zoomOutFloatingActionButton = findViewById(R.id.activity_tune_zoomout_floatingactionbutton);
            recyclerView = findViewById(R.id.activity_tune_recyclerview);

            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
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
                TextView setNameTextView = findViewById(R.id.recyclerview_footer_setName_textView);
                setNameTextView.setText(setEntity.setName);
                setNameTextView.setVisibility(VISIBLE);
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
            zoomInFloatingActionButton.setOnClickListener(this);
            zoomOutFloatingActionButton.setOnClickListener(this);
            findViewById(R.id.activity_tune_back_floatingactionbutton).setOnClickListener(this);

            TextWatcher titleTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup);
            TextWatcher tagTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup);
            TextWatcher composerTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneComposersAutoCompleteTextView, tuneComposersChipGroup);
            TextWatcher regionsOfOriginTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneRegionsOfOriginAutoCompleteTextView, tuneRegionsOfOriginChipGroup);
            TextWatcher keyTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tuneKeysAutoCompleteTextView, tuneKeysChipGroup);
            TextWatcher playedByTextWatcher = new ChipGroupUtilities.CustomTextWatcher(this, this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup);
            //Prepare the autocompletes
            tuneTitlesAutoCompleteTextView.setThreshold(0);
            tuneTitlesAutoCompleteTextView.addTextChangedListener(titleTextWatcher);
            tuneTagsAutoCompleteTextView.setThreshold(0);
            tuneTagsAutoCompleteTextView.addTextChangedListener(tagTextWatcher);
            tuneComposersAutoCompleteTextView.setThreshold(0);
            tuneComposersAutoCompleteTextView.addTextChangedListener(composerTextWatcher);
            tuneRegionsOfOriginAutoCompleteTextView.setThreshold(0);
            tuneRegionsOfOriginAutoCompleteTextView.addTextChangedListener(regionsOfOriginTextWatcher);
            tuneKeysAutoCompleteTextView.setThreshold(0);
            tuneKeysAutoCompleteTextView.addTextChangedListener(keyTextWatcher);
            tuneIncipitAutoCompleteTextView.setThreshold(0);
            tuneFormAutoCompleteTextView.setThreshold(0);
            tunePlayedByAutoCompleteTextView.setThreshold(0);
            tunePlayedByAutoCompleteTextView.addTextChangedListener(playedByTextWatcher);
            tuneNoteAutoCompleteTextView.setThreshold(0);
            prepareAutocompleteAdapters();

            //Display the data
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTitles, DEFAULT_SEPARATOR), tuneTitlesChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneTags, DEFAULT_SEPARATOR), tuneTagsChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneComposers, DEFAULT_SEPARATOR), tuneComposersChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneRegionsOfOrigin, DEFAULT_SEPARATOR), tuneRegionsOfOriginChipGroup);
            ChipGroupUtilities.addChipsToChipGroup(this, StringUtils.split(tuneEntity.tuneKeys, DEFAULT_SEPARATOR), tuneKeysChipGroup);
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
            adjustZoomButtonsVisibility();
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
            tuneEntity.tuneTitles = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tuneTitlesAutoCompleteTextView, tuneTitlesChipGroup);
            tuneEntity.tuneTags = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tuneTagsAutoCompleteTextView, tuneTagsChipGroup);
            tuneEntity.tuneComposers = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tuneComposersAutoCompleteTextView, tuneComposersChipGroup);
            tuneEntity.tuneRegionsOfOrigin = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tuneRegionsOfOriginAutoCompleteTextView, tuneRegionsOfOriginChipGroup);
            tuneEntity.tuneKeys = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tuneKeysAutoCompleteTextView, tuneKeysChipGroup);
            tuneEntity.tuneIncipit = tuneIncipitAutoCompleteTextView.getText().toString();
            tuneEntity.tuneForm = tuneFormAutoCompleteTextView.getText().toString();
            tuneEntity.tunePlayedBy = ChipGroupUtilities.retrieveChipsFromChipGroup(this, tunePlayedByAutoCompleteTextView, tunePlayedByChipGroup);
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
            ServiceSingleton.getInstance().interruptTuneActivityDataRetrieval();
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
            ServiceSingleton.getInstance().prepareTuneActivityData(this, this, tuneEntity, setEntity, position, tuneOrSet);
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
            if (view.getId() == R.id.activity_tune_back_floatingactionbutton) {
                this.finish();
            } else if (view.getId() == R.id.activity_tune_zoomin_floatingactionbutton) {
                zoom(ZOOM_INCREMENTS);
            } else if (view.getId() == R.id.activity_tune_zoomout_floatingactionbutton) {
                zoom(-ZOOM_INCREMENTS);
            } else if (view.getId() == R.id.activity_tune_edit_floatingactionbutton) {
                drawerLayout.openDrawer(GravityCompat.END);
            } else if (view.getId() == R.id.tune_nav_header_save_button) {
                saveTune();
            } else if (view.getId() == R.id.tune_nav_header_back_floatingactionbutton) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else if (view.getId() == R.id.recyclerview_footer_set_textView) {
                displayPopupMenuOfSetsWithTune(view);
            } else if (view.getId() == R.id.recyclerview_footer_tunesByComposers_textView) {
                displayPopupMenuOfTunesByComposers(view);
            } else if (view.getId() == R.id.recyclerview_footer_tuneSuggestions_textView) {
                displayDialogOfTuneSuggestions();
            } else if (view.getId() == R.id.recyclerview_footer_innerbuttonprevious_constraintlayout) {
                loadPreviousTune();
            } else if (view.getId() == R.id.recyclerview_footer_innerbuttonnext_constraintlayout) {
                loadNextTune();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
        }
    }

    private void zoom(float increment) {
        currentZoom += increment;
        adjustZoomButtonsVisibility();
        tunePagesRecyclerViewAdapter.zoom(currentZoom);
    }

    private void adjustZoomButtonsVisibility() {
        if (currentZoom >= ZOOM_MAX) {
            zoomInFloatingActionButton.setVisibility(INVISIBLE);
        } else {
            zoomInFloatingActionButton.setVisibility(VISIBLE);
        }
        if (currentZoom <= ZOOM_MIN) {
            zoomOutFloatingActionButton.setVisibility(INVISIBLE);
        } else {
            zoomOutFloatingActionButton.setVisibility(VISIBLE);
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

    private void displayPopupMenuOfTunesByComposers(View view) {
        try {
            if (tuneOrSet == TuneOrSet.set) {
                return;
            }
            if (StaticData.tuneByComposersList == null || StaticData.tuneByComposersList.isEmpty()) {
                return;
            }
            PopupMenu popupMenu = new PopupMenu(this, view);
            for (int i = 0, max = StaticData.tuneByComposersList.size(); i < max; i++) {
                popupMenu.getMenu().add(NONE, i, NONE, StaticData.tuneByComposersList.get(i).tuneTitles.split(DEFAULT_SEPARATOR)[0]);
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        Utilities.loadActivity(activity, context, TuneActivity.class, new Pair[]{
                                new Pair<>(OPERATION, TuneOrSet.tune),
                                new Pair<>(TUNE_ENTITY, StaticData.tuneByComposersList.get(menuItem.getItemId())),
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
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured while displaying tunes by same composers.", e));
        }
    }

    private void displayDialogOfTuneSuggestions() {
        try {
            if (tuneOrSet == TuneOrSet.set) {
                return;
            }
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.tunesuggestions_dialog);
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
            dialog.findViewById(R.id.tunesuggestions_dialog_back_floatingActionButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            String tuneLastKey = Utilities.getTuneLastKey(tuneEntity);
            ((TextView)dialog.findViewById(R.id.tunesuggestions_dialog_currentkey_textview)).setText("Current key: " + tuneLastKey);
            TextView tunesuggestions_dialog_minustoneminor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_minustoneminor_textview);
            TextView tunesuggestions_dialog_minustonemajor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_minustonemajor_textview);
            TextView tunesuggestions_dialog_relativeminor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_relativeminor_textview);
            TextView tunesuggestions_dialog_relativemajor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_relativemajor_textview);
            TextView tunesuggestions_dialog_plustoneminor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plustoneminor_textview);
            TextView tunesuggestions_dialog_plustonemajor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plustonemajor_textview);
            TextView tunesuggestions_dialog_plusfourthminor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plusfourthminor_textview);
            TextView tunesuggestions_dialog_plusfourthmajor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plusfourthmajor_textview);
            TextView tunesuggestions_dialog_plusfifthminor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plusfifthminor_textview);
            TextView tunesuggestions_dialog_plusfifthmajor_textview = dialog.findViewById(R.id.tunesuggestions_dialog_plusfifthmajor_textview);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_minustoneminor_textview, R.string.minus_1_minor, tuneLastKey, -2, KeyQualifier.minor);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_minustonemajor_textview, R.string.minus_1_major, tuneLastKey, -2, KeyQualifier.major);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_relativeminor_textview, R.string.relative_minor, tuneLastKey, 0, KeyQualifier.minor);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_relativemajor_textview, R.string.relative_major, tuneLastKey, 0, KeyQualifier.major);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plustoneminor_textview, R.string.plus_1_minor, tuneLastKey, +2, KeyQualifier.minor);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plustonemajor_textview, R.string.plus_1_major, tuneLastKey, +2, KeyQualifier.major);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plusfourthminor_textview, R.string.plus_4_minor, tuneLastKey, -1, KeyQualifier.minor);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plusfourthmajor_textview, R.string.plus_4_major, tuneLastKey, -1, KeyQualifier.major);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plusfifthminor_textview, R.string.plus_5_minor, tuneLastKey, +1, KeyQualifier.minor);
            setTuneSuggestionTextViewText(tunesuggestions_dialog_plusfifthmajor_textview, R.string.plus_5_major, tuneLastKey, +1, KeyQualifier.major);
            activateTuneSuggestionTextView(tunesuggestions_dialog_minustoneminor_textview, StaticData.tuneSuggestions.minusToneMinor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_minustonemajor_textview, StaticData.tuneSuggestions.minusToneMajor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_relativeminor_textview, StaticData.tuneSuggestions.relativeMinor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_relativemajor_textview, StaticData.tuneSuggestions.relativeMajor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plustoneminor_textview, StaticData.tuneSuggestions.plusToneMinor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plustonemajor_textview, StaticData.tuneSuggestions.plusToneMajor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plusfourthminor_textview, StaticData.tuneSuggestions.plusFourthMinor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plusfourthmajor_textview, StaticData.tuneSuggestions.plusFourthMajor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plusfifthminor_textview, StaticData.tuneSuggestions.plusFifthMinor);
            activateTuneSuggestionTextView(tunesuggestions_dialog_plusfifthmajor_textview, StaticData.tuneSuggestions.plusFifthMajor);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An error occured while displaying the tune suggestions dialog.", e));
        }
    }

    private void setTuneSuggestionTextViewText(TextView textView, int baseStringResId, String currentTuneLastKey, int desiredInterval, KeyQualifier desiredKeyQualifier) {
        String keyWithInterval = null;
        try {
            keyWithInterval = Utilities.getKeyWithInterval(currentTuneLastKey, desiredInterval, desiredKeyQualifier);
        } catch (Exception e) {
            //Do nothing
        }
        textView.setText(getString(baseStringResId, keyWithInterval == null ? "--" : keyWithInterval));
    }

    private void activateTuneSuggestionTextView(TextView textView, TuneEntity suggestedTuneEntity) {
        if (suggestedTuneEntity != null) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_purple));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Utilities.loadActivity(activity, context, TuneActivity.class, new Pair[]{
                                new Pair<>(OPERATION, TuneOrSet.tune),
                                new Pair<>(TUNE_ENTITY, suggestedTuneEntity),
                                new Pair<>(CLICK_TYPE, Constants.ClickType.shortClick.toString())
                        });
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An error occured during an onClick event.", e));
                    }
                }
            });
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
        tuneComposersAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneComposerArray));
        tuneComposersAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tuneComposersAutoCompleteTextView, tuneComposersChipGroup));
        tuneRegionsOfOriginAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneRegionArray));
        tuneRegionsOfOriginAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tuneRegionsOfOriginAutoCompleteTextView, tuneRegionsOfOriginChipGroup));
        tuneKeysAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueTuneKeyArray));
        tuneKeysAutoCompleteTextView.setOnItemClickListener(new ChipGroupUtilities.CustomOnItemClickListener(this, this, tuneKeysAutoCompleteTextView, tuneKeysChipGroup));
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
                    } else if (TUNES_BY_COMPOSERS.equals(broadcastValue)) {
                        displayTunesByComposers();
                    } else if (TUNES_SUGGESTIONS.equals(broadcastValue)) {
                        displayTunesSuggestions();
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
            previousTuneButton.setVisibility(VISIBLE);
            previousTuneButton.setOnClickListener(this);
            ((TextView)findViewById(R.id.recyclerview_footer_previousfooter_textview)).setText(StaticData.previousTune.getFirstTitle());
        }
        if (StaticData.nextTune != null) {
            View nextTuneButton = findViewById(R.id.recyclerview_footer_innerbuttonnext_constraintlayout);
            nextTuneButton.setVisibility(VISIBLE);
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
        setTextView.setVisibility(VISIBLE);
        setTextView.setOnClickListener(this);
    }

    private void displayTunesByComposers() {
        if (tuneOrSet == TuneOrSet.set) {
            return;
        }
        if (StaticData.tuneByComposersList == null || StaticData.tuneByComposersList.isEmpty()) {
            return;
        }
        TextView tunesByComposersTextView = findViewById(R.id.recyclerview_footer_tunesByComposers_textView);
        tunesByComposersTextView.setVisibility(VISIBLE);
        tunesByComposersTextView.setOnClickListener(this);
    }

    private void displayTunesSuggestions() {
        if (tuneOrSet == TuneOrSet.set) {
            return;
        }
        if (StaticData.tuneSuggestions == null || !StaticData.tuneSuggestions.hasSuggestion()) {
            return;
        }
        TextView tunesByComposersTextView = findViewById(R.id.recyclerview_footer_tuneSuggestions_textView);
        tunesByComposersTextView.setVisibility(VISIBLE);
        tunesByComposersTextView.setOnClickListener(this);
    }

    private void retrieveBitmaps() {
        if (StaticData.bitmapList != null) {
            tunePagesRecyclerViewAdapter = new TunePagesRecyclerViewAdapter(StaticData.bitmapList);
            recyclerView.setAdapter(tunePagesRecyclerViewAdapter);
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
