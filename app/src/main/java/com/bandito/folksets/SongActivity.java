package com.bandito.folksets;

import static java.util.Objects.isNull;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.google.android.material.navigation.NavigationView;

import java.time.OffsetDateTime;

public class SongActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SongActivity.class.getName();
    private Constants.SongOrSet songOrSet;
    private int position;
    private SetEntity setEntity;
    private SongEntity songEntity;
    private DrawerLayout drawerLayout;
    private TextView songTitlesTextView;
    private TextView songTagsTextView;
    private TextView songFilePathTextView;
    private TextView songFileTypeTextView;
    private TextView songComposerTextView;
    private TextView songRegionOfOriginTextView;
    private TextView songKeyTextView;
    private TextView songIncipitTextView;
    private TextView songFormTextView;
    private TextView songPlayedByTextView;
    private TextView songNoteTextView;
    private TextView songFileCreationDateTextView;
    private TextView songLastConsultationDateTextView;
    private TextView songConsultationNumberTextView;

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

        //Display song data
        if (!isNull(songEntity)) {
            View headerView = ((NavigationView)findViewById(R.id.song_nav_view)).getHeaderView(0);
            songTitlesTextView = headerView.findViewById(R.id.nav_song_title_textview);
            songTitlesTextView.setText(songEntity.songTitles);
            songTagsTextView = headerView.findViewById(R.id.nav_song_tags_textview);
            songTagsTextView.setText(songEntity.songTags);
            songFilePathTextView = headerView.findViewById(R.id.nav_song_filePath_textview);
            songFilePathTextView.setText(songEntity.songFilePath);
            songFileTypeTextView = headerView.findViewById(R.id.nav_song_fileType_textview);
            songFileTypeTextView.setText(songEntity.songFileType);
            songComposerTextView = headerView.findViewById(R.id.nav_song_composer_textview);
            songComposerTextView.setText(songEntity.songComposer);
            songRegionOfOriginTextView = headerView.findViewById(R.id.nav_song_region_textview);
            songRegionOfOriginTextView.setText(songEntity.songRegionOfOrigin);
            songKeyTextView = headerView.findViewById(R.id.nav_song_key_textview);
            songKeyTextView.setText(songEntity.songKey);
            songIncipitTextView = headerView.findViewById(R.id.nav_song_incipit_textview);
            songIncipitTextView.setText(songEntity.songIncipit);
            songFormTextView = headerView.findViewById(R.id.nav_song_form_textview);
            songFormTextView.setText(songEntity.songForm);
            songPlayedByTextView = headerView.findViewById(R.id.nav_song_players_textview);
            songPlayedByTextView.setText(songEntity.songPlayedBy);
            songNoteTextView = headerView.findViewById(R.id.nav_song_note_textview);
            songNoteTextView.setText(songEntity.songNote);
            songFileCreationDateTextView = headerView.findViewById(R.id.nav_song_creation_date_textview);
            songFileCreationDateTextView.setText(songEntity.songFileCreationDate);
            songLastConsultationDateTextView = headerView.findViewById(R.id.nav_song_consultation_date_textview);
            songLastConsultationDateTextView.setText(songEntity.songLastConsultationDate);
            songConsultationNumberTextView = headerView.findViewById(R.id.nav_song_consultation_number_textview);
            songConsultationNumberTextView.setText(songEntity.songConsultationNumber.toString());
        }

        drawerLayout = findViewById(R.id.drawer_layout_song);
        String clickType = getIntent().getExtras().getString(Constants.CLICK_TYPE);
        if (!isNull(clickType)) {
            Log.i(TAG, "Activity entered via " + clickType + ".");
            if (Constants.ClickType.longClick.toString().equals(clickType)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        }

        findViewById(R.id.edit_song_fab).setOnClickListener(this);
        findViewById(R.id.back_song_fab).setOnClickListener(this);
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