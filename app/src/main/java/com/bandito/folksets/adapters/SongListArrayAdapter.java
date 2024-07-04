package com.bandito.folksets.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import com.bandito.folksets.sql.entities.SongEntity;

import java.util.List;

public class SongListArrayAdapter extends ArrayAdapter<SongEntity> {

    public SongListArrayAdapter(@NonNull Context context, int resource, @NonNull List<SongEntity> songEntityList) {
        super(context, resource, songEntityList);
    }
}
