package com.example.smartgymroom;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment {

    private Activity activity;
    private ActivityDatabase db;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = requireActivity();

        View view = inflater.inflate(R.layout.fragmant_history, container, false);
        listView = view.findViewById(R.id.list_view);

        db = new ActivityDatabase(activity);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListView();
    }

    private void setupListView() {
        Cursor cursor = db.getLimitedActivities(5);
        String[] fromColumns = {"duration", "activity_type", "date"};
        int[] toViews = {R.id.textViewDuration, R.id.textViewActivityType, R.id.textViewDate};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                requireContext(),
                R.layout.list_item,
                cursor,
                fromColumns,
                toViews,
                0
        );
        listView.setAdapter(adapter);
    }


}
