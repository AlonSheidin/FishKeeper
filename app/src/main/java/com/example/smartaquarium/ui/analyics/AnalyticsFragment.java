package com.example.smartaquarium.ui.analyics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.viewModel.AquariumViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;


import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment  {

    private LineChart lineChart;
    private LineDataSet dataSet; // Keep reference so we can append
    private LineData lineData;

    private AquariumViewModel viewModel;

    public AnalyticsFragment() {}


    private int n = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AquariumViewModel.class);
        lineChart = root.findViewById(R.id.line_chart);

        // Shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AquariumViewModel.class);
        setupObservers();



        return root;
    }
    private void setupObservers() {
        // 1. Load full history once
        ArrayList<AquariumData> history = (ArrayList<AquariumData>) viewModel.getHistory().getValue();
        if (dataSet == null) { // Only initialize once
            List<Entry> entries = new ArrayList<>();
            for (AquariumData data : history) {
                entries.add(new Entry(n, data.temperature));
                n++;
            }
            dataSet = new LineDataSet(entries, "Temperature");
            dataSet.setColor(Color.RED);
            dataSet.setCircleColor(Color.RED);

            lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            lineChart.invalidate();
        }


        // 2. Append only new points
        viewModel.getLatestData().observe(getViewLifecycleOwner(), data -> {
            if (dataSet != null && lineData != null) {
                Entry entry = new Entry(n, data.temperature);
                dataSet.addEntry(entry);
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate(); // redraw with appended point
                n++;
            }
        });
    }


}
