package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.SET_ENTITY;
import static com.bandito.folksets.util.Constants.TUNE_ID;
import static com.bandito.folksets.util.Constants.TUNE_TITLES;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bandito.folksets.adapters.TuneListArrayAdapter;
import com.bandito.folksets.adapters.TuneListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetActivity extends AppCompatActivity {

    private static final String TAG = SetActivity.class.getName();
    private Constants.SetOperation currentSetOperation;
    private SetEntity currentSet = new SetEntity();
    private TextInputEditText setNameEditTextInputEditText;
    private RecyclerView recyclerView;
    private TuneListRecyclerViewAdapter recyclerViewAdapter;
    private View lastSelectedTuneSelectedView = null;
    private Drawable lastSelectedTuneSelectedDrawable = null;
    private Integer selectedTuneSelectionPosition = null;
    private Activity activity;
    private Context context;
    private Dialog dialog;

    private final TuneListRecyclerViewAdapter.ItemClickListener selectedTuneItemClickListener = new TuneListRecyclerViewAdapter.ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            try {
                if (view.equals(lastSelectedTuneSelectedView)) {
                    lastSelectedTuneSelectedView.setBackground(lastSelectedTuneSelectedDrawable);
                    lastSelectedTuneSelectedView = null;
                    lastSelectedTuneSelectedDrawable = null;
                    selectedTuneSelectionPosition = null;
                } else {
                    if (lastSelectedTuneSelectedView != null) {
                        lastSelectedTuneSelectedView.setBackground(lastSelectedTuneSelectedDrawable);
                    }
                    lastSelectedTuneSelectedView = view;
                    lastSelectedTuneSelectedDrawable = view.getBackground();
                    selectedTuneSelectionPosition = position;
                    view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.view_border, context.getTheme()));
                }
            } catch (Exception e) {
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an OnItemClick event.", e));
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            try {
                onItemClick(view, position);
            } catch (Exception e) {
                ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an OnLongItemClick event.", e));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_set_constraintlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            activity = this;
            context = this;

            //Prepare the RecyclerView and its adapter for selected tunes
            recyclerView = findViewById(R.id.activity_set_recyclerview);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            recyclerViewAdapter = new TuneListRecyclerViewAdapter(this, this);
            recyclerViewAdapter.setClickListener(selectedTuneItemClickListener);

            //Retrieve the bundle message
            setNameEditTextInputEditText = findViewById(R.id.activity_set_name_textinputedittext);
            currentSetOperation = (Constants.SetOperation)getIntent().getExtras().getSerializable(OPERATION);
            if (currentSetOperation == Constants.SetOperation.editSet) {
                currentSet = (SetEntity) getIntent().getExtras().getSerializable(SET_ENTITY);
                ((TextView) findViewById(R.id.activity_set_header_textview)).setText(R.string.edit_set);
                setNameEditTextInputEditText.setText(currentSet.setName);
                String[] currentSetTuneIdArrayOfStrings = StringUtils.split(currentSet.setTunes, DEFAULT_SEPARATOR);
                if (currentSetTuneIdArrayOfStrings != null) {
                    List<TuneEntity> unorderedCurrentSetTuneEntityList = DatabaseManager.findTunesByIdInDatabase(TUNE_ID + "," + TUNE_TITLES, currentSetTuneIdArrayOfStrings, null, null);
                    List<TuneEntity> orderedCurrentSetTuneEntityList = new ArrayList<>();
                    for (String tuneId : currentSetTuneIdArrayOfStrings) {
                        orderedCurrentSetTuneEntityList.add(unorderedCurrentSetTuneEntityList.stream().filter(tuneEntity -> tuneEntity.tuneId.equals(Long.valueOf(tuneId))).findFirst().get());
                    }
                    recyclerViewAdapter.setTuneEntityList(orderedCurrentSetTuneEntityList);
                }
            } else {
                findViewById(R.id.activity_set_delete_button).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.activity_set_header_textview)).setText(R.string.create_new_set);
            }

            recyclerView.setAdapter(recyclerViewAdapter);
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured during the OnCreate step of class SetActivity.", e, true));
        }
    }

    public void onBackButtonClick(View view) {
        try {
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnBackButtonClick event.", e));
        }
    }

    public void onDeleteSetButtonClick(View view) {
        try {
            DatabaseManager.removeSetFromDatabase(currentSet.setId);
            Toast.makeText(this, "Set deleted", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnDeleteSetButtonClick event.", e));
        }
    }

    public void onSaveSetButtonClick(View view) {
        try {
            currentSet.setName = setNameEditTextInputEditText.getText().toString();
            if (currentSet.setName.isEmpty()) {
                Toast.makeText(this, "Choose a name for the set", Toast.LENGTH_SHORT).show();
                return;
            }
            currentSet.setTunes = StringUtils.join(
                    recyclerViewAdapter.getTuneEntityList().stream().map(tuneEntity -> tuneEntity.tuneId).collect(Collectors.toList()),
                    DEFAULT_SEPARATOR
            );
            if (currentSet.setTunes.isEmpty()) {
                Toast.makeText(this, "Select at least one tune", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentSetOperation == Constants.SetOperation.createSet) {
                DatabaseManager.insertSetInDatabase(currentSet);
            } else {
                DatabaseManager.updateSetInDatabase(currentSet);
            }
            Toast.makeText(this, "Set saved", Toast.LENGTH_SHORT).show();
            this.finish();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while processing an OnSaveSetButtonClick event.", e));
        }
    }

    private void selectTune(TuneEntity tuneEntity) {
        try {
            recyclerViewAdapter.addTuneEntity(tuneEntity);
            recyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while selecting a tune.", e));
        }
    }

    public void deselectTune(View view) {
        try {
            if (selectedTuneSelectionPosition != null) {
                lastSelectedTuneSelectedView.setBackground(lastSelectedTuneSelectedDrawable);
                recyclerViewAdapter.removeTuneEntity(selectedTuneSelectionPosition);
                recyclerViewAdapter.notifyItemRemoved(selectedTuneSelectionPosition);
                lastSelectedTuneSelectedView = null;
                lastSelectedTuneSelectedDrawable = null;
                selectedTuneSelectionPosition = null;
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while deselecting a tune.", e));
        }
    }

    public void moveTuneUp(View view) {
        try {
            if (selectedTuneSelectionPosition != null && selectedTuneSelectionPosition > 0) {
                lastSelectedTuneSelectedView.setBackground(lastSelectedTuneSelectedDrawable);
                recyclerViewAdapter.addTuneEntityAtIndex(selectedTuneSelectionPosition - 1, recyclerViewAdapter.removeTuneEntity(selectedTuneSelectionPosition));
                recyclerViewAdapter.notifyDataSetChanged();
                lastSelectedTuneSelectedView = null;
                lastSelectedTuneSelectedDrawable = null;
                int newPosition = selectedTuneSelectionPosition - 1;
                selectedTuneSelectionPosition = null;
                selectedTuneItemClickListener.onItemClick(recyclerView.getChildAt(newPosition), newPosition);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while moving a tune up.", e));
        }
    }

    public void moveTuneDown(View view) {
        try {
            if (selectedTuneSelectionPosition != null && selectedTuneSelectionPosition < recyclerViewAdapter.getItemCount() - 1) {
                lastSelectedTuneSelectedView.setBackground(lastSelectedTuneSelectedDrawable);
                recyclerViewAdapter.addTuneEntityAtIndex(selectedTuneSelectionPosition + 1, recyclerViewAdapter.removeTuneEntity(selectedTuneSelectionPosition));
                recyclerViewAdapter.notifyDataSetChanged();
                lastSelectedTuneSelectedView = null;
                lastSelectedTuneSelectedDrawable = null;
                int newPosition = selectedTuneSelectionPosition + 1;
                selectedTuneSelectionPosition = null;
                selectedTuneItemClickListener.onItemClick(recyclerView.getChildAt(newPosition), newPosition);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while moving a tune down.", e));
        }
    }

    public void openSelectTuneDialog(View view) {
        try {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.searchable_spinner);
            int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
            int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
            ((TextView)dialog.findViewById(R.id.searchable_spinner_textview)).setText(R.string.select_a_tune);
            ((EditText)dialog.findViewById(R.id.searchable_spinner_edittext)).setHint(R.string.search_tunes);
            dialog.findViewById(R.id.searchable_spinner_back_floatingActionButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            ListView listView = dialog.findViewById(R.id.searchable_spinner_listview);
            TuneListArrayAdapter arrayAdapter = new TuneListArrayAdapter(this, android.R.layout.simple_list_item_1, StaticData.tuneEntityList);
            listView.setAdapter(arrayAdapter);
            ((EditText)dialog.findViewById(R.id.searchable_spinner_edittext)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    try {
                        arrayAdapter.getFilter().filter(charSequence);
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while processing an OnTextChanged event.", e));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        selectTune(arrayAdapter.getItem(position));
                        dialog.dismiss();
                    } catch (Exception e) {
                        ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured wile processing an OnItemClick event.", e));
                    }
                }
            });
        } catch (Exception e) {
            ExceptionManager.manageException(this, this, TAG, new FolkSetsException("An exception occured while opening a tune selection dialog.", e));
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
}
