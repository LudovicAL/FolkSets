package com.bandito.folksets;

import android.os.Bundle;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bandito.folksets.adapters.SetListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;

import java.util.List;

public class SetListFragment extends Fragment implements SetListRecyclerViewAdapter.ItemClickListener {

    private ProgressBar progressBar;
    public SetListRecyclerViewAdapter setListRecyclerViewAdapter;

    public SetListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_list, container, false);
        progressBar = view.findViewById(R.id.setListProgressBar);
        RecyclerView recyclerView = view.findViewById(R.id.setListRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        setListRecyclerViewAdapter = new SetListRecyclerViewAdapter();
        setListRecyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(setListRecyclerViewAdapter);
        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", Constants.SET_ID, String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i("TAG", "You short clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SongActivity.class, new Pair[]{
                    new Pair<>(Constants.SET_ENTITY, setEntityList.get(0)),
                    new Pair<>(Constants.CLICK_TYPE, Constants.ClickType.shortClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        try {
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", Constants.SET_ID, String.valueOf(setListRecyclerViewAdapter.getItem(position)), null, null);
            Log.i("TAG", "You long clicked " + setEntityList.get(0).setName + ", which is at cell position " + position);
            Utilities.loadActivity(requireActivity(), requireContext(), SongActivity.class, new Pair[]{
                    new Pair<>(Constants.SET_ENTITY, setEntityList.get(0)),
                    new Pair<>(Constants.CLICK_TYPE, Constants.ClickType.longClick.toString())
            });
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }
}