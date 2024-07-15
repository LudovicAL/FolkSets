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

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.IntentLauncher;
import com.bandito.folksets.util.Utilities;

import org.apache.commons.lang3.StringUtils;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SettingsFragment.class.getName();
    private TextView selectStorageDirectoryTextView;
    private ConstraintLayout cropperInnerConstraintLayout;
    private final CompoundButton.OnCheckedChangeListener cropperActivationSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                Utilities.writeBooleanToSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, isChecked);
                if (cropperInnerConstraintLayout != null) {
                    if (isChecked) {
                        cropperInnerConstraintLayout.setVisibility(View.VISIBLE);
                    } else {
                        cropperInnerConstraintLayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An error occured while processing cropper switch checked change.", e));
            }
        }
    };
    private final SeekBar.OnSeekBarChangeListener cropperSensitivitySeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                Utilities.writeIntToSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, progress);
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing cropper seekbar change.", e));
            }
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
        try {
            view.findViewById(R.id.fragment_settings_selectstoragedirectory_button).setOnClickListener(this);
            view.findViewById(R.id.fragment_settings_exportdatabase_button).setOnClickListener(this);
            view.findViewById(R.id.fragment_settings_importdatabase_button).setOnClickListener(this);
            selectStorageDirectoryTextView = view.findViewById(R.id.fragment_settings_selectstoragedirectory_textview);
            cropperInnerConstraintLayout = view.findViewById(R.id.fragment_setting_cropperinner_constraintlayout);
            updateSelectStorageDirectoryTextView();
            SeekBar seekBar = view.findViewById(R.id.fragment_settings_pdfcropper_seekbar);
            seekBar.setProgress(Utilities.readIntFromSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, CROPPER_DEFAULT_VALUE));
            seekBar.setOnSeekBarChangeListener(cropperSensitivitySeekBarListener);
            SwitchCompat switchCompat = view.findViewById(R.id.fragment_setting_croppingactivation_switch);
            switchCompat.setChecked(Utilities.readBooleanFromSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, CROPPER_DEFAULT_ACTIVATION));
            switchCompat.setOnCheckedChangeListener(cropperActivationSwitchListener);
            switchCompat.toggle();
            switchCompat.toggle();
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured during the OnCreateView step of class SettingsFragment.", e, true));
        }
        return view;
    }

    private void updateSelectStorageDirectoryTextView() {
        try {
            String directory = Utilities.readStringFromSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, null);
            if (StringUtils.isEmpty(directory)) {
                selectStorageDirectoryTextView.setVisibility(View.GONE);
            } else {
                selectStorageDirectoryTextView.setText(directory);
                selectStorageDirectoryTextView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while updating the selected storage directory textview.", e));
        }
    }

    public void selectStorageDirectory() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            intentLauncher.launch(intent, activityResult -> selectFolderCallback(activityResult));
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while selecting the storage directory.", e));
        }
    }

    private void selectFolderCallback(ActivityResult activityResult) {
        try {
            if (activityResult.getResultCode() == Activity.RESULT_OK && activityResult.getData() != null) {
                Intent resultData = activityResult.getData();
                Uri uriTree = resultData.getData();
                if (uriTree == null) {
                    Log.e(TAG, "The folder picking intent returned a null object.");
                } else {
                    final int modeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireActivity().getContentResolver().takePersistableUriPermission(uriTree, modeFlags);
                    Utilities.writeStringToSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, uriTree.toString());
                    updateSelectStorageDirectoryTextView();
                }
            } else {
                throw new FolkSetsException("The folder picking intent failed.", null);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing the select folder intent callback.", e));
        }
    }

    public void exportDatabaseToStorage() {
        try {
            DatabaseManager.exportDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Exportation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while exporting the database to storage.", e));
        }
    }

    public void importDatabaseFromStorage() {
        try {
            DatabaseManager.importDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Importation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while importing the database from storage.", e));
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.fragment_settings_selectstoragedirectory_button) {
                selectStorageDirectory();
            } else if (view.getId() == R.id.fragment_settings_exportdatabase_button) {
                exportDatabaseToStorage();
            } else if (view.getId() == R.id.fragment_settings_importdatabase_button) {
                importDatabaseFromStorage();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
        }
    }
}