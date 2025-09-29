package com.example.smartaquarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.util.ArrayList;

public class AnalyticsFragment extends Fragment {

    private LineChart lineChart;
    private RadioGroup rgTimeFilter;

    public AnalyticsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);

        lineChart = root.findViewById(R.id.line_chart);
        rgTimeFilter = root.findViewById(R.id.rg_time_filter);

        // Example data (replace with Firebase data)
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 25f)); // time, temperature
        entries.add(new Entry(1, 26f));
        entries.add(new Entry(2, 24.5f));
        entries.add(new Entry(3, 25.2f));

        LineDataSet dataSet = new LineDataSet(entries, "Temperature °C");

        dataSet.setValueTextColor(getResources().getColor(R.color.black));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh chart

        rgTimeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            // TODO: Load filtered data from Firebase
        });

        return root;
    }
}
