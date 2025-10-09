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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment  {

    private LineChart lineChart;
    private LineDataSet dataSet; // Keep reference so we can append
    private LineData lineData;

    private AquariumViewModel viewModel;

    public AnalyticsFragment() {}



    private static class EntryCount {
        int temperature;
        int ph;
        int oxygen;
        int waterLevel;

        EntryCount(int temperature, int ph, int oxygen, int waterLevel) {
            this.temperature = temperature;
            this.ph = ph;
            this.oxygen = oxygen;
            this.waterLevel = waterLevel;
        }
    }    private final EntryCount entryCount = new EntryCount(0, 0, 0, 0);

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
        List<AquariumData> history = viewModel.getHistory().getValue();
        if(history == null) history = new ArrayList<>(); // Handle null case
        else history = new ArrayList<>(history); // Make mutable copy

        if (dataSet == null) { // Only initialize once
            List<Entry> entries = new ArrayList<>();
            for (AquariumData data : history) {
                entries.add(new Entry(entryCount.temperature, data.temperature));
                entryCount.temperature++;
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
                Entry entry = new Entry(entryCount.temperature, data.temperature);
                dataSet.addEntry(entry);
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate(); // redraw with appended point
                entryCount.temperature++;
            }
        });
    }



}
