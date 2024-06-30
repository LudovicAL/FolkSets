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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bandito.folksets.adapters.TabAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewPager2 viewPager2 = findViewById(R.id.viewpager2);
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), getLifecycle());
        tabAdapter.addFragment(new SongListFragment());
        tabAdapter.addFragment(new SetListFragment());
        tabAdapter.addFragment(new SettingsFragment());
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setAdapter(tabAdapter);
        tabLayout = findViewById(R.id.tabsLayout);
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_songs);
                    break;
                case 1:
                    tab.setText(R.string.tab_sets);
                    break;
                case 2:
                    tab.setText(R.string.tab_settings);
                    break;
            }
        }).attach();

        try {
            DatabaseManager.initializeDatabase(this);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }

        checkStorageDirectory();
    }

    public void checkStorageDirectory() {
        String selectedDirectoryUri = Utilities.readStringFromSharedPreferences(this, Constants.STORAGE_DIRECTORY_URI, null);
        if (isNull(selectedDirectoryUri) && tabLayout.getSelectedTabPosition() != 2) {
            tabLayout.selectTab(tabLayout.getTabAt(2));
        }
    }

    @Override
    protected void onDestroy() {
        try {
            DatabaseManager.closeDatabase();
        } catch (Exception e) {
            Log.e(TAG, "An error occured while closing the database.", e);
        }
        super.onDestroy();
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