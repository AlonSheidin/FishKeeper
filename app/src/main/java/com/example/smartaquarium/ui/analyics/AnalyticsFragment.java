package com.example.smartaquarium.ui.analyics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    // A constant to define the default number of visible entries
    private static final int MAX_VISIBLE_ENTRIES = 15;

    /**
     * Enum to represent the different data types that can be displayed.
     */
    private enum DataType {
        TEMPERATURE,
        PH,
        OXYGEN,
        WATER_LEVEL
    }

    private LineChart lineChart;
    private Spinner dataTypeSpinner;
    private AquariumViewModel aquariumViewModel;

    private DataType currentDataType = DataType.TEMPERATURE; // Default to temperature
    private List<AquariumData> historyData; // Cache for the full history

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        setupDataObservers();
    }

    /**
     * Initializes all views and objects for the first time.
     */
    private void init(View view) {
        // Initialize views
        lineChart = view.findViewById(R.id.line_chart);
        dataTypeSpinner = view.findViewById(R.id.spinner_data_type);

        // Initialize ViewModel
        aquariumViewModel = new ViewModelProvider(requireActivity()).get(AquariumViewModel.class);

        // Set up UI components
        setupChartStyling();
        setupSpinner();
    }

    private void setupChartStyling() {
        lineChart.getDescription().setEnabled(false); // Disable description text
        lineChart.setTouchEnabled(true); // Enable touch gestures
        lineChart.setPinchZoom(true); // Allow zooming
        lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_ENTRIES); // Show only a limited number of entries at once
    }

    private void setupSpinner() {
        // Create an adapter for the spinner from the DataType enum
        ArrayAdapter<DataType> adapter = new ArrayAdapter<>(
                requireContext(), // Use context from the fragment
                android.R.layout.simple_spinner_item, // Use a standard layout
                DataType.values() // Use enum values directly
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Set dropdown layout
        dataTypeSpinner.setAdapter(adapter); // Attach the adapter to the spinner

        // Set a listener to react to user selections
        dataTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the newly selected data type
                DataType selectedType = (DataType) parent.getItemAtPosition(position);

                // If the selection has changed, update the chart
                if (selectedType != currentDataType) {
                    currentDataType = selectedType;
                    // Redraw the entire chart with the new data type
                    if (historyData != null) {
                        populateInitialChart(historyData);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupDataObservers() {
        aquariumViewModel.getHistory().observe(getViewLifecycleOwner(), history -> {
            if (history != null && !history.isEmpty()) {
                // Cache the history and populate the chart
                this.historyData = history;
                populateInitialChart(history);
            }
        });

        aquariumViewModel.getLatestData().observe(getViewLifecycleOwner(), newData -> {
            if (newData != null && lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
                appendDataToChart(newData);
            }
        });
    }

    private void populateInitialChart(@NonNull List<AquariumData> history) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            AquariumData data = history.get(i);
            // Get the correct value based on the currentDataType
            float value = getValueForDataType(data, currentDataType);
            entries.add(new Entry(i, value));
        }

        // Get label and color for the selected data type
        String label = getLabelForDataType(currentDataType);
        int color = getColorForDataType(currentDataType);

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_ENTRIES);
        lineChart.moveViewToX(lineData.getEntryCount());
        // Refresh the chart to apply changes
        lineChart.invalidate();
    }

    private void appendDataToChart(@NonNull AquariumData data) {
        LineData lineData = lineChart.getData();
        if (lineData == null) return;
        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
        if (dataSet == null) return;

        // Get the correct value based on the current selection
        float value = getValueForDataType(data, currentDataType);
        Entry newEntry = new Entry(dataSet.getEntryCount(), value);

        lineData.addEntry(newEntry, 0);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();

        lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_ENTRIES);
        lineChart.moveViewToX(lineData.getEntryCount());
    }

    /** Helper method to get the correct data value from AquariumData. */
    private float getValueForDataType(AquariumData data, DataType type) {
        return switch (type) {
            case TEMPERATURE -> data.getTemperature();
            case PH -> data.getPh();
            case OXYGEN -> data.getOxygen();
            case WATER_LEVEL -> data.getWaterLevel();
        };
    }

    /** Helper method to get a display label for the dataset. */
    private String getLabelForDataType(DataType type) {
        return switch (type) {
            case TEMPERATURE -> "Temperature";
            case PH -> "pH Level";
            case OXYGEN -> "Oxygen";
            case WATER_LEVEL -> "Water Level";
        };
    }

    /** Helper method to get a color for the dataset. */
    private int getColorForDataType(DataType type) {
        return switch (type) {
            case TEMPERATURE -> Color.RED;
            case PH -> Color.BLUE;
            case OXYGEN -> Color.GREEN;
            case WATER_LEVEL -> Color.CYAN;
        };
    }
}
