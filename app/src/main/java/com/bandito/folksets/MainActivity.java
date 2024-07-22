package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.LOGFILE_DEFAULT_ACTIVATION;
import static com.bandito.folksets.util.Constants.LOGFILE_PREFERED_ACTIVATION_KEY;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bandito.folksets.adapters.TabAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.services.ServiceSingleton;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView progressBarHintTextView;
    private Activity activity;
    private final MainActivity.MyBroadcastReceiver myBroadcastReceiver = new MainActivity.MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main_constraintlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            activity = this;
            if (Utilities.readBooleanFromSharedPreferences(this, LOGFILE_PREFERED_ACTIVATION_KEY, LOGFILE_DEFAULT_ACTIVATION)) {
                IoUtilities.createNewLogFile(this, this);
            }
            progressBar = findViewById(R.id.activity_main_progressbar);
            progressBarHintTextView = findViewById(R.id.activity_main_progressbar_hint_textView);
            ViewPager2 viewPager2 = findViewById(R.id.activity_main_viewpager2);
            TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), getLifecycle());
            tabAdapter.addFragment(new TuneListFragment());
            tabAdapter.addFragment(new SetListFragment());
            tabAdapter.addFragment(new SettingsFragment());
            viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            viewPager2.setUserInputEnabled(false);
            viewPager2.setAdapter(tabAdapter);
            tabLayout = findViewById(R.id.activity_main_tablayout);
            new TabLayoutMediator(tabLayout, viewPager2,
                    (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(R.string.tab_tunes);
                        break;
                    case 1:
                        tab.setText(R.string.tab_sets);
                        break;
                    case 2:
                        tab.setText(R.string.tab_settings);
                        break;
                }
            }).attach();
            DatabaseManager.initializeDatabase(this);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured during the OnCreate step of class MainActivity.", e, true));
        }
    }

    @Override
    protected void onDestroy() {
        try {
            ServiceSingleton.getInstance().interruptDatabaseUpdate();
            DatabaseManager.closeDatabase();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while destroying MainActivity.", e, true));
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            String selectedDirectoryUri = Utilities.readStringFromSharedPreferences(this, STORAGE_DIRECTORY_URI, null);
            if (selectedDirectoryUri == null && tabLayout.getSelectedTabPosition() != 2) {
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.mainActivityProgressUpdate.toString()));
                ServiceSingleton.getInstance().UpdateDatabase(this, this);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while resuming MainActivity.", e, true));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while pausing MainActivity.", e, true));
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

    private void updateProgressBar(int progressNumericValue, String progressTextHint) {
        progressBar.setProgress(progressNumericValue);
        progressBarHintTextView.setText(progressTextHint);
    }

    private void displayProgressBar(int visibility) {
        progressBarHintTextView.setVisibility(visibility);
        progressBar.setVisibility(visibility);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getExtras() == null) {
                    return;
                }
                if (intent.getExtras().containsKey(Constants.BroadcastKey.progressValue.toString())) {
                    updateProgressBar(intent.getExtras().getInt(Constants.BroadcastKey.progressValue.toString()), intent.getExtras().getString(Constants.BroadcastKey.progressHint.toString()));
                }
                if (intent.getExtras().containsKey(Constants.BroadcastKey.progressVisibility.toString())) {
                    displayProgressBar(intent.getExtras().getInt(Constants.BroadcastKey.progressVisibility.toString()));
                }
            } catch (Exception e) {
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured during an OnReceive event.", e));
            }
        }
    }
}
