package com.bandito.folksets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SetActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SetActivity.class.getName();
    private Constants.SetOperation currentSetOperation;
    private SetEntity currentSet = new SetEntity();
    private EditText setNameEditText;

    List<SongEntity> allSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.set), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setNameEditText = findViewById(R.id.set_name_edittext);

        try {
            allSongs = DatabaseManager.findSongsInDatabase("*", null, null, Constants.SONG_TITLES, null);
        } catch (FolkSetsException e) {
            Log.e(TAG, "An error occured when retrieving the list of all songs.", e);
        }

        if (Constants.SetOperation.editSet.toString().equals(getIntent().getExtras().getString(Constants.OPERATION))) {
            currentSetOperation = Constants.SetOperation.editSet;
            currentSet = (SetEntity) getIntent().getExtras().getSerializable(Constants.SET_ENTITY);
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.edit_set);
            setNameEditText.setText(currentSet.setName);
        } else {
            currentSetOperation = Constants.SetOperation.createSet;
            ((TextView)findViewById(R.id.set_activity_header)).setText(R.string.create_new_set);
        }

        findViewById(R.id.back_set_fab).setOnClickListener(this);
        findViewById(R.id.button_save_set).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_set_fab) {
            this.finish();
        } else if (view.getId() == R.id.button_save_set) {
            saveSet();
        }
    }

    private void saveSet() {
        try {
            currentSet.setName = setNameEditText.getText().toString();
            if (currentSetOperation == Constants.SetOperation.createSet) {
                DatabaseManager.insertSetInDatabase(currentSet);
            } else {
                DatabaseManager.updateSetInDatabase(currentSet);
            }
            Toast.makeText(this, "Set saved", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(e);
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