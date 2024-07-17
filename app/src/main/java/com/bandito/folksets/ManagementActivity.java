package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.OPERATION;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.util.Constants;

public class ManagementActivity extends AppCompatActivity {

    private static final String TAG = ManagementActivity.class.getName();
    private Constants.ManagementOperation tagsOrPlayers;

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
            tagsOrPlayers = (Constants.ManagementOperation)getIntent().getExtras().getSerializable(OPERATION);
            ((TextView)findViewById(R.id.activity_management_header_textview)).setText(tagsOrPlayers == Constants.ManagementOperation.manageTags ? "Manage tags" : "Manage players");

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

        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnCreateNewButtonClick event.", e));
        }
    }

    public void openSelectDialog(View view) {
        try {

        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OpenSelectDialog event.", e));
        }
    }
}