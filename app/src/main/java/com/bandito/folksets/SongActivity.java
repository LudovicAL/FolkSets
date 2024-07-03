package com.bandito.folksets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.google.android.material.navigation.NavigationView;

import java.time.OffsetDateTime;

public class SongActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SongActivity.class.getName();
    private Constants.SongOrSet songOrSet;
    private int position;
    private SetEntity setEntity;
    private SongEntity songEntity;
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView songTitlesAutoCompleteTextView;
    private AutoCompleteTextView songTagsAutoCompleteTextView;
    private AutoCompleteTextView songComposerAutoCompleteTextView;
    private AutoCompleteTextView songRegionOfOriginAutoCompleteTextView;
    private AutoCompleteTextView songKeyAutoCompleteTextView;
    private AutoCompleteTextView songIncipitAutoCompleteTextView;
    private AutoCompleteTextView songFormAutoCompleteTextView;
    private AutoCompleteTextView songPlayedByAutoCompleteTextView;
    private AutoCompleteTextView songNoteAutoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_song);

        //Determine what was received: a song or a set
        String songOrSetStr = getIntent().getExtras().getString(Constants.OPERATION);
        songOrSet = Constants.SongOrSet.set.toString().equals(songOrSetStr) ? Constants.SongOrSet.set : Constants.SongOrSet.song;

        //Retrieve Song data
        if (songOrSet.equals(Constants.SongOrSet.song)) {
            songEntity = (SongEntity) getIntent().getExtras().getSerializable(Constants.SONG_ENTITY);
        } else {
            setEntity = (SetEntity) getIntent().getExtras().getSerializable(Constants.SET_ENTITY);
            position = getIntent().getExtras().getInt(Constants.POSITION);
            try {
                songEntity = DatabaseManager.findSongByIdInDatabase("*", setEntity.getSong(position), null, null).get(0);
            } catch (Exception e) {
                Log.e(TAG, "An error occured while fetching a song at position " + position + " in set.", e);
            }
        }

        View headerView = ((NavigationView)findViewById(R.id.song_nav_view)).getHeaderView(0);
        //Set listeners
        headerView.findViewById(R.id.back_from_edit_song_fab).setOnClickListener(this);
        headerView.findViewById(R.id.button_save_song).setOnClickListener(this);
        findViewById(R.id.edit_song_fab).setOnClickListener(this);
        findViewById(R.id.back_song_fab).setOnClickListener(this);

        //Prepare the autocompletes
        ArrayAdapter<String> songTitleAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongTitleArray);
        songTitlesAutoCompleteTextView = headerView.findViewById(R.id.nav_song_title_textview);
        songTitlesAutoCompleteTextView.setAdapter(songTitleAdapter);
        songTitlesAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songTagAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongTagArray);
        songTagsAutoCompleteTextView = headerView.findViewById(R.id.nav_song_tags_textview);
        songTagsAutoCompleteTextView.setAdapter(songTagAdapter);
        songTagsAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songComposerAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongComposerArray);
        songComposerAutoCompleteTextView = headerView.findViewById(R.id.nav_song_composer_textview);
        songComposerAutoCompleteTextView.setAdapter(songComposerAdapter);
        songComposerAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songRegionAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongRegionArray);
        songRegionOfOriginAutoCompleteTextView = headerView.findViewById(R.id.nav_song_region_textview);
        songRegionOfOriginAutoCompleteTextView.setAdapter(songRegionAdapter);
        songRegionOfOriginAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songKeyAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongKeyArray);
        songKeyAutoCompleteTextView = headerView.findViewById(R.id.nav_song_key_textview);
        songKeyAutoCompleteTextView.setAdapter(songKeyAdapter);
        songKeyAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songIncipitAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongIncipitArray);
        songIncipitAutoCompleteTextView = headerView.findViewById(R.id.nav_song_incipit_textview);
        songIncipitAutoCompleteTextView.setAdapter(songIncipitAdapter);
        songIncipitAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songFormAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongFormArray);
        songFormAutoCompleteTextView = headerView.findViewById(R.id.nav_song_form_textview);
        songFormAutoCompleteTextView.setAdapter(songFormAdapter);
        songFormAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songPlayedByAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongPlayedByArray);
        songPlayedByAutoCompleteTextView = headerView.findViewById(R.id.nav_song_players_textview);
        songPlayedByAutoCompleteTextView.setAdapter(songPlayedByAdapter);
        songPlayedByAutoCompleteTextView.setThreshold(0);

        ArrayAdapter<String> songNoteAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, StaticData.uniqueSongNoteArray);
        songNoteAutoCompleteTextView = headerView.findViewById(R.id.nav_song_note_textview);
        songNoteAutoCompleteTextView.setAdapter(songNoteAdapter);
        songNoteAutoCompleteTextView.setThreshold(0);

        //Display the data
        try {
            songTitlesAutoCompleteTextView.setText(songEntity.songTitles);
            songTagsAutoCompleteTextView.setText(songEntity.songTags);
            songComposerAutoCompleteTextView.setText(songEntity.songComposer);
            songRegionOfOriginAutoCompleteTextView.setText(songEntity.songRegionOfOrigin);
            songKeyAutoCompleteTextView.setText(songEntity.songKey);
            songIncipitAutoCompleteTextView.setText(songEntity.songIncipit);
            songFormAutoCompleteTextView.setText(songEntity.songForm);
            songPlayedByAutoCompleteTextView.setText(songEntity.songPlayedBy);
            songNoteAutoCompleteTextView.setText(songEntity.songNote);
            ((TextView)headerView.findViewById(R.id.nav_song_filePath_textview)).setText(songEntity.songFilePath);
            ((TextView)headerView.findViewById(R.id.nav_song_fileType_textview)).setText(songEntity.songFileType);
            ((TextView)headerView.findViewById(R.id.nav_song_creation_date_textview)).setText(songEntity.songFileCreationDate);
            ((TextView)headerView.findViewById(R.id.nav_song_consultation_date_textview)).setText(songEntity.songLastConsultationDate);
            ((TextView)headerView.findViewById(R.id.nav_song_consultation_number_textview)).setText(songEntity.songConsultationNumber.toString());
        } catch (Exception e) {
            ExceptionManager.manageException(this, e);
        }

        drawerLayout = findViewById(R.id.drawer_layout_song);
        String clickType = getIntent().getExtras().getString(Constants.CLICK_TYPE);
        if (Constants.ClickType.longClick.toString().equals(clickType)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void saveSong() {
        songEntity.songTitles = songTitlesAutoCompleteTextView.getText().toString();
        if (songEntity.songTitles.isEmpty()) {
            Toast.makeText(this, "A title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        songEntity.songTags = songTagsAutoCompleteTextView.getText().toString();
        songEntity.songComposer = songComposerAutoCompleteTextView.getText().toString();
        songEntity.songRegionOfOrigin = songRegionOfOriginAutoCompleteTextView.getText().toString();
        songEntity.songKey = songKeyAutoCompleteTextView.getText().toString();
        songEntity.songIncipit = songIncipitAutoCompleteTextView.getText().toString();
        songEntity.songForm = songFormAutoCompleteTextView.getText().toString();
        songEntity.songPlayedBy = songPlayedByAutoCompleteTextView.getText().toString();
        songEntity.songNote = songNoteAutoCompleteTextView.getText().toString();
        try {
            DatabaseManager.updateSongInDatabase(songEntity);
            Toast.makeText(this, "Song saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            ExceptionManager.manageException(this, e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            songEntity.songLastConsultationDate = OffsetDateTime.now().toString();
            DatabaseManager.updateSongInDatabase(songEntity);
        } catch (Exception e) {
            Log.e(TAG, "And error occured when trying to update the song last consultation date.", e);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.edit_song_fab) {
            drawerLayout.openDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.back_from_edit_song_fab) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (view.getId() == R.id.button_save_song) {
            saveSong();
        } else if (view.getId() == R.id.back_song_fab) {
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
}