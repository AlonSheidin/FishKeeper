package com.example.smartaquarium.data.viewModel.analyics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.Transformations; // Import Transformations

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.viewModel.aquariumData.AquariumDataViewModel;
import com.example.smartaquarium.ui.analyics.AnalyticsFragment;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Analytics screen. This ViewModel is responsible for all
 * data processing and state management for the analytics chart.
 * It creates a reactive data pipeline that observes the raw data from AquariumDataViewModel
 * and the user's UI selections, transforming them into the final chart data.
 */
public class AnalyticsViewModel extends AndroidViewModel {

    // --- Input LiveData ---
    // This holds the user's choice from the Spinner in the UI.
    private final MutableLiveData<AnalyticsFragment.DataType> selectedDataType = new MutableLiveData<>();

    // --- Output LiveData ---
    // This is the ONLY LiveData the fragment needs to observe. It holds the final chart data.
    private final LiveData<LineData> processedChartData;

    /**
     * Constructor for the ViewModel.
     * The init() method is called here to set up the reactive data streams.
     */
    public AnalyticsViewModel(@NonNull Application application, @NonNull ViewModelStoreOwner owner) {
        super(application);

        // Get the single, shared instance of the ViewModel that provides raw data.
        AquariumDataViewModel aquariumDataViewModel = new ViewModelProvider(owner).get(AquariumDataViewModel.class);

        // Call the initialization method to set up our data processing pipeline.
        processedChartData = init(aquariumDataViewModel);
    }

    /**
     * Initializes the reactive data processing pipeline.
     * This method uses Transformations to link the raw data and user selections to the final chart data.
     *
     * @param dataProviderViewModel The ViewModel that supplies the raw aquarium data.
     * @return A LiveData stream that will always hold the most up-to-date chart data.
     */
    private LiveData<LineData> init(AquariumDataViewModel dataProviderViewModel) {
        // We have two "trigger" sources: the historical data and the user's data type selection.
        LiveData<List<AquariumData>> historyDataSource = dataProviderViewModel.getHistory();

        // Use Transformations.switchMap to react to changes in the historical data.
        // If the user logs in/out, historyDataSource will change, and this will re-trigger.
        return Transformations.switchMap(historyDataSource, history ->
                // Once we have a history list, use Transformations.map to react to changes
                // in the user's data type selection.
                Transformations.map(selectedDataType, dataType -> {
                    // This inner function is a pure "converter". It takes the raw history and the selected
                    // data type and converts them into the final LineData for the chart.
                    return processDataForChart(history, dataType);
                })
        );
    }

    /**
     * Exposes the final, processed chart data to be observed by the Fragment.
     */
    public LiveData<LineData> getProcessedChartData() {
        return processedChartData;
    }

    /**
     * Called by the Fragment when the user selects a new data type from the spinner.
     * This updates the selectedDataType LiveData, which automatically triggers the
     * transformation pipeline to re-process the chart data.
     */
    public void setDataType(AnalyticsFragment.DataType dataType) {
        // Only update if the value is new to prevent redundant calculations.
        if (dataType != selectedDataType.getValue()) {
            selectedDataType.setValue(dataType);
        }
    }

    /**
     * The core logic that processes raw data into chart-ready LineData.
     * This is a pure function: its output depends only on its inputs.
     *
     * @param history The raw list of aquarium data.
     * @param dataType The currently selected data type for the chart.
     * @return The final, styled LineData object ready for the UI.
     */
    private LineData processDataForChart(List<AquariumData> history, AnalyticsFragment.DataType dataType) {
        // If the user hasn't selected a type yet, default to Temperature.
        if (dataType == null) {
            dataType = AnalyticsFragment.DataType.TEMPERATURE;
        }

        // If there's no history data, there's nothing to draw.
        if (history == null || history.isEmpty()) {
            return new LineData(); // Return an empty LineData to clear the chart
        }

        // 1. Convert raw data into chart Entries
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            AquariumData data = history.get(i);
            int value = dataType.getValue(data);
            chartEntries.add(new Entry(i, value));
        }

        // 2. Create and style the DataSet
        LineDataSet chartDataSet = new LineDataSet(chartEntries, dataType.toString());
        int color = dataType.getColor(getApplication().getApplicationContext());
        chartDataSet.setColor(color);
        chartDataSet.setCircleColor(color);
        chartDataSet.setDrawValues(false);

        // 3. Return the final LineData object
        return new LineData(chartDataSet);
    }
}
