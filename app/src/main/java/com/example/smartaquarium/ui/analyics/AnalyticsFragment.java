package com.example.smartaquarium.ui.analyics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.viewModel.AquariumDataViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link Fragment} that displays historical aquarium data in a line chart.
 *
 * <p>This fragment visualizes various sensor readings (like temperature, pH, etc.) over time.
 * It uses the {@link AquariumDataViewModel} to observe live data updates and historical records.
 * A {@link Spinner} allows the user to select which data type to display on the chart. The chart
 * is powered by the MPAndroidChart library.</p>
 *
 * <p>The fragment handles:
 * <ul>
 *     <li>Initializing and styling the {@link LineChart}.</li>
 *     <li>Populating a {@link Spinner} with available data types from the {@code DataType} enum.</li>
 *     <li>Observing historical data to populate the chart initially.</li>
 *     <li>Observing the latest data point to append it to the chart in real-time.</li>
 *     <li>Updating the chart when the user selects a different data type from the spinner.</li>
 * </ul>
 * </p>
 */
public class AnalyticsFragment extends Fragment {

    // A constant to define the default number of visible entries
    private static final int MAX_VISIBLE_ENTRIES = 15;

    /**
     * Represents the different types of aquarium data that can be displayed in the chart.
     *
     * <p>Each enum constant holds metadata for a specific data type, including:
     * <ul>
     *     <li>A user-friendly {@code label} for display in UI components like Spinners.</li>
     *     <li>A {@code colorResId} to consistently style the chart for that data type.</li>
     *     <li>A {@code valueExtractor} function to retrieve the correct integer value from an
     *     {@link AquariumData} object.</li>
     * </ul>
     * This design encapsulates the logic for handling different data streams, making it easy
     * to add new data types in the future.
     */
    private enum DataType {
        TEMPERATURE("Temperature", R.color.chart_temperature, AquariumData::getTemperature),
        PH("pH Level", R.color.chart_ph, AquariumData::getPh),
        OXYGEN("Oxygen Level", R.color.chart_oxygen, AquariumData::getOxygen),
        WATER_LEVEL("Water Level", R.color.chart_water_level, AquariumData::getWaterLevel);

        private final String label;
        @ColorRes
        private final int colorResId;
        private final Function<AquariumData, Integer> valueExtractor;

        /**
         * Constructs a new DataType enum constant.
         *
         * @param label The user-friendly name for the data type, used in the UI (e.g., "Temperature").
         * @param colorResId The resource ID of the color to use for this data type's chart line.
         * @param valueExtractor A function that takes an AquariumData object and returns the specific integer value for this data type (e.g., `AquariumData::getTemperature`).
         */
        DataType(String label, @ColorRes int colorResId, Function<AquariumData, Integer> valueExtractor) {
            this.label = label;
            this.colorResId = colorResId;
            this.valueExtractor = valueExtractor;
        }

        /**
         * Extracts the relevant integer value from an AquariumData object.
         * This method uses the function defined for the enum constant to retrieve
         * the correct data point (e.g., temperature, pH).
         *
         * @param data The AquariumData object from which to extract the value.
         * @return The integer value corresponding to this data type.*/
        public int getValue(AquariumData data) {
            return valueExtractor.apply(data);
        }

        /**
         * Gets the resolved color integer for this data type.
         *
         * @param context The context used to resolve the color resource.
         * @return The resolved ARGB color integer.
         */
        public int getColor(@NonNull Context context) {
            return ContextCompat.getColor(context, this.colorResId);
        }

        /**
         * Returns the user-friendly label for the data type.
         *
         * @return The string representation of the enum, which is its label.
         */
        @NonNull
        @Override
        public String toString() {
            return this.label;
        }
    }

    private LineChart lineChart;
    private Spinner dataTypeSpinner;
    private AquariumDataViewModel aquariumDataViewModel;

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
        aquariumDataViewModel = new ViewModelProvider(requireActivity()).get(AquariumDataViewModel.class);

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
                    if (historyData != null && !historyData.isEmpty()) {
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
        aquariumDataViewModel.getHistory().observe(getViewLifecycleOwner(), history -> {
            if (history != null && !history.isEmpty()) {
                // Cache the history and populate the chart
                this.historyData = history;
                populateInitialChart(history);
            }
        });

        aquariumDataViewModel.getLatestData().observe(getViewLifecycleOwner(), newData -> {
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
            int value = currentDataType.getValue(data);
            entries.add(new Entry(i, value));
        }

        // Get label and color for the selected data type
        String label = currentDataType.toString();
        int color = currentDataType.getColor(requireContext());

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawValues(false);

        lineChart.setData(new LineData(dataSet));
        moveChartViewToLastEntry();
        // Refresh the chart to apply changes
        lineChart.invalidate();
    }

    private void appendDataToChart(@NonNull AquariumData data) {
        LineData lineData = lineChart.getData();
        if (lineData == null) throw new IllegalStateException("DataSet should not be null");
        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
        if (dataSet == null) throw new IllegalStateException("DataSet should not be null");

        // Get the correct value based on the current selection
        int value = currentDataType.getValue(data);
        Entry newEntry = new Entry(dataSet.getEntryCount(), value);

        lineData.addEntry(newEntry, 0);
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();

        moveChartViewToLastEntry();
    }

    /** Helper method to move the chart view to the last entry. */
    private void moveChartViewToLastEntry() {
        // Set the maximum number of visible entries.
        lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_ENTRIES);
        // Move the viewport to the end of the chart.
        lineChart.moveViewToX(lineChart.getData().getEntryCount());
    }
}
