package com.example.smartaquarium.ui.aquarium;

import android.animation.ValueAnimator;
import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.viewModel.aquarium.AquariumViewModel;
import com.example.smartaquarium.data.viewModel.aquarium.AquariumViewModelFactory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.HashMap;
import java.util.Map;

public class AquariumFragment extends Fragment {

    // --- Views and ViewModel ---
    private BarChart barChartTemperature;
    private BarChart barChartPh;
    private BarChart barChartOxygen;
    private BarChart barChartWaterLevel;
    private TextView textViewLastUpdate;

    private AquariumViewModel aquariumViewModel;

    private final Map<BarChart, Float> lastChartValues = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aquarium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        observeViewModel();
    }

    /**
     * Initializes all views and the ViewModel for the first time.
     */
    private void initialize(@NonNull View view) {
        // Bind views from the layout
        barChartTemperature = view.findViewById(R.id.barChart_temperature);
        barChartPh = view.findViewById(R.id.barChart_ph);
        barChartOxygen = view.findViewById(R.id.barChart_oxygen);
        barChartWaterLevel = view.findViewById(R.id.barChart_waterLevel);
        textViewLastUpdate = view.findViewById(R.id.textView_lastUpdate);

        // Style the charts
        styleBarChart(barChartTemperature);
        styleBarChart(barChartPh);
        styleBarChart(barChartOxygen);
        styleBarChart(barChartWaterLevel);

        // Get the ViewModel using its factory
        Application application = requireActivity().getApplication();
        FragmentActivity owner = requireActivity();
        AquariumViewModelFactory factory = new AquariumViewModelFactory(application, owner);
        aquariumViewModel = new ViewModelProvider(this, factory).get(AquariumViewModel.class);
    }

    /**
     * Sets up observers on the ViewModel's LiveData to react to data changes.
     */
    private void observeViewModel() {
        // Observe the BarData for temperature
        aquariumViewModel.temperatureBarData.observe(getViewLifecycleOwner(), barData ->
            updateChart(barChartTemperature, barData)
        );

        // Observe the BarData for pH
        aquariumViewModel.phBarData.observe(getViewLifecycleOwner(), barData ->
            updateChart(barChartPh, barData)
        );

        // Observe the BarData for oxygen
        aquariumViewModel.oxygenBarData.observe(getViewLifecycleOwner(), barData ->
            updateChart(barChartOxygen, barData)
        );

        // Observe the BarData for water level
        aquariumViewModel.waterLevelBarData.observe(getViewLifecycleOwner(), barData ->
            updateChart(barChartWaterLevel, barData)
        );

        // Observe the timestamp string
        aquariumViewModel.lastUpdatedTimestamp.observe(getViewLifecycleOwner(), timestampText ->
            textViewLastUpdate.setText(timestampText)
        );
    }

    /**
     * A helper method to apply common styling to a BarChart.
     */
    private void styleBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setFitBars(true);
        chart.setTouchEnabled(false); // Disable interaction for a display-only chart

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        // Make sure the Y-axis redraws properly
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);

        chart.getAxisLeft().setAxisMaximum(100f);    // End at 100
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
    }

    /**
     * A helper method to update a chart's data and refresh it.
     */
    private void updateChart(BarChart chart, BarData newBarData) {
        if (newBarData == null || newBarData.getDataSetCount() == 0) {
            chart.clear();
            return;
        }

        // Get the target value from the new data
        float newValue = newBarData.getYMax();

        // Get the starting value from our map, defaulting to 0f if not present
        float previousValue = lastChartValues.getOrDefault(chart, 0f);

        // Create a value animator that goes from the previous value to the new one
        ValueAnimator animator = ValueAnimator.ofFloat(previousValue, newValue);
        animator.setDuration(800); // Animation duration in milliseconds

        animator.addUpdateListener(animation -> {
            // This listener is called for every frame of the animation
            float animatedValue = (float) animation.getAnimatedValue();

            // Get the chart's data set
            BarDataSet dataSet = (BarDataSet) newBarData.getDataSetByIndex(0);
            // Get the entry (the bar)
            BarEntry entry = dataSet.getEntryForIndex(0);
            // Update the entry's Y value to the current animated value
            entry.setY(animatedValue);

            // Update the chart with the modified data and redraw it
            chart.setData(newBarData);
            chart.invalidate();
        });

        // Start the animation
        animator.start();

        // Store the new value as the "last value" for the next animation
        lastChartValues.put(chart, newValue);
    }
}
