package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_ACTIVATION;
import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_VALUE;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_ACTIVATION_KEY;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_VALUE_KEY;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.IntentLauncher;
import com.bandito.folksets.util.Utilities;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SettingsFragment.class.getName();
    private TextView selectStorageDirectoryTextView;
    private ConstraintLayout innerConstraintLayout;
    private final CompoundButton.OnCheckedChangeListener cropperActivationSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Utilities.writeBooleanToSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, isChecked);
            if (innerConstraintLayout != null) {
                if (isChecked) {
                    innerConstraintLayout.setVisibility(View.VISIBLE);
                } else {
                    innerConstraintLayout.setVisibility(View.GONE);
                }
            }
        }
    };
    private final SeekBar.OnSeekBarChangeListener cropperSensitivitySeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Utilities.writeIntToSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final IntentLauncher<Intent, ActivityResult> intentLauncher = IntentLauncher.registerActivityForResult(this);

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.fragment_settings_selectstoragedirectory_button).setOnClickListener(this);
        view.findViewById(R.id.fragment_settings_exportdatabase_button).setOnClickListener(this);
        view.findViewById(R.id.fragment_settings_importdatabase_button).setOnClickListener(this);
        selectStorageDirectoryTextView = view.findViewById(R.id.fragment_settings_selectstoragedirectory_textview);
        innerConstraintLayout = view.findViewById(R.id.fragment_setting_inner_constraintlayout);
        updateSelectStorageDirectoryTextView(null);
        SeekBar seekBar = view.findViewById(R.id.fragment_settings_pdfcropper_seekbar);
        seekBar.setProgress(Utilities.readIntFromSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, CROPPER_DEFAULT_VALUE));
        seekBar.setOnSeekBarChangeListener(cropperSensitivitySeekBarListener);
        SwitchCompat switchCompat = view.findViewById(R.id.fragment_setting_croppingactivation_switch);
        switchCompat.setChecked(Utilities.readBooleanFromSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, CROPPER_DEFAULT_ACTIVATION));
        switchCompat.setOnCheckedChangeListener(cropperActivationSwitchListener);
        switchCompat.toggle();
        switchCompat.toggle();
        return view;
    }

    private void updateSelectStorageDirectoryTextView(String text) {
        if (text == null) {
            text = Utilities.readStringFromSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, getString (R.string.select_storage_directory));
        }
        selectStorageDirectoryTextView.setText(text);
    }

    public void selectStorageDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intentLauncher.launch(intent, activityResult -> selectFolderCallback(activityResult));
    }

    private void selectFolderCallback(ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK && activityResult.getData() != null) {
            Intent resultData = activityResult.getData();
            Uri uriTree = resultData.getData();
            if (uriTree == null) {
                Log.e(TAG, "The folder picking intent returned a null object.");
            } else {
                final int modeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                requireActivity().getContentResolver().takePersistableUriPermission(uriTree, modeFlags);
                Utilities.writeStringToSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, uriTree.toString());
                updateSelectStorageDirectoryTextView(null);
            }
        } else {
            Log.e(TAG, "The folder picking intent failed.");
        }
    }

    public void exportDatabaseToStorage() {
        try {
            DatabaseManager.exportDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Exportation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "An error occured", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "An error occured when exporting the database to storage.", e);
        }
    }

    public void importDatabaseFromStorage() {
        try {
            DatabaseManager.importDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Importation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "An error occured", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "An error occured when importing the database from storage.", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() ==  R.id.fragment_settings_selectstoragedirectory_button) {
            selectStorageDirectory();
        } else if (view.getId() ==  R.id.fragment_settings_exportdatabase_button) {
            exportDatabaseToStorage();
        } else if (view.getId() ==  R.id.fragment_settings_importdatabase_button) {
            importDatabaseFromStorage();
        }
    }
}