package com.bandito.folksets.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import com.bandito.folksets.sql.entities.TuneEntity;

import java.util.List;

public class TuneListArrayAdapter extends ArrayAdapter<TuneEntity> {

    public TuneListArrayAdapter(@NonNull Context context, int resource, @NonNull List<TuneEntity> tuneEntityList) {
        super(context, resource, tuneEntityList);
    }
}
