package com.example.smartaquarium.data.viewModel.analyics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.MediatorLiveData;

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.viewModel.aquariumData.AquariumDataViewModel;
import com.example.smartaquarium.ui.analyics.AnalyticsFragment;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for the Analytics screen. This ViewModel is responsible for all
 * data processing and state management for the analytics chart.
 */
public class AnalyticsViewModel extends AndroidViewModel {

    // --- Input LiveData ---
    private final MutableLiveData<AnalyticsFragment.DataType> selectedDataType = new MutableLiveData<>();
    private final MutableLiveData<AnalyticsFragment.DateFilter> selectedDateFilter = new MutableLiveData<>();

    // --- Output LiveData ---
    private final LiveData<LineData> processedChartData;

    public AnalyticsViewModel(@NonNull Application application, @NonNull ViewModelStoreOwner owner) {
        super(application);
        AquariumDataViewModel aquariumDataViewModel = new ViewModelProvider(owner).get(AquariumDataViewModel.class);

        // Default values to ensure the pipeline triggers
        selectedDataType.setValue(AnalyticsFragment.DataType.TEMPERATURE);
        selectedDateFilter.setValue(AnalyticsFragment.DateFilter.LAST_24_HOURS);

        processedChartData = init(aquariumDataViewModel);
    }

    private LiveData<LineData> init(AquariumDataViewModel dataProviderViewModel) {
        LiveData<List<AquariumData>> historyDataSource = dataProviderViewModel.getHistory();
        MediatorLiveData<LineData> mediator = new MediatorLiveData<>();

        // Helper to re-process whenever any input changes
        Runnable updatePipeline = () -> {
            List<AquariumData> history = historyDataSource.getValue();
            AnalyticsFragment.DataType type = selectedDataType.getValue();
            AnalyticsFragment.DateFilter filter = selectedDateFilter.getValue();

            if (history != null && type != null && filter != null) {
                mediator.setValue(processDataForChart(history, type, filter));
            }
        };

        mediator.addSource(historyDataSource, h -> updatePipeline.run());
        mediator.addSource(selectedDataType, t -> updatePipeline.run());
        mediator.addSource(selectedDateFilter, f -> updatePipeline.run());

        return mediator;
    }

    public LiveData<LineData> getProcessedChartData() {
        return processedChartData;
    }

    public void setDataType(AnalyticsFragment.DataType dataType) {
        if (dataType != selectedDataType.getValue()) {
            selectedDataType.setValue(dataType);
        }
    }

    public void setDateFilter(AnalyticsFragment.DateFilter filter) {
        if (filter != selectedDateFilter.getValue()) {
            selectedDateFilter.setValue(filter);
        }
    }
    private final MutableLiveData<List<String>> userAquariumsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> selectedAquariumId = new MutableLiveData<>();

    // 1. Provide the list of aquariums to the Fragment
    public LiveData<List<String>> getUserAquariumsList() {
        return userAquariumsList;
    }

    // 2. Provide the currently selected ID
    public LiveData<String> getSelectedAquariumId() {
        return selectedAquariumId;
    }

    // 3. Logic to change the selected aquarium
    public void setSelectedAquarium(String aquariumId) {
        selectedAquariumId.setValue(aquariumId);
        // Add logic here to reload 'latestData' for the specific aquariumId
    }

    // 4. Logic to add a new aquarium
    public void createNewAquarium(String name) {
        List<String> currentList = userAquariumsList.getValue();
        if (currentList != null) {
            currentList.add(name);
            userAquariumsList.setValue(currentList);
        }
    }
    private LineData processDataForChart(List<AquariumData> history, AnalyticsFragment.DataType dataType, AnalyticsFragment.DateFilter filter) {
        if (history == null || history.isEmpty()) {
            return new LineData();
        }

        // 1. Filter by Date
        List<AquariumData> filteredList = filterHistoryByDate(history, filter);

        if (filteredList.isEmpty()) {
            return new LineData();
        }

        // 2. Convert to chart Entries
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < filteredList.size(); i++) {
            AquariumData data = filteredList.get(i);
            int value = dataType.getValue(data);
            chartEntries.add(new Entry(i, value));
        }

        // 3. Create and style the DataSet
        LineDataSet chartDataSet = new LineDataSet(chartEntries, dataType.toString() + " (" + filter.toString() + ")");
        int color = dataType.getColor(getApplication().getApplicationContext());
        chartDataSet.setColor(color);
        chartDataSet.setCircleColor(color);
        chartDataSet.setDrawValues(false);

        return new LineData(chartDataSet);
    }

    /**
     * Filters the list of aquarium history data based on a specified time range.
     *
     * @param history The complete list of {@link AquariumData} to be filtered.
     * @param filter The time duration filter (e.g., last 24 hours, last week) to apply.
     * @return A list of {@link AquariumData} records that fall within the specified time frame.
     */
    private List<AquariumData> filterHistoryByDate(List<AquariumData> history, AnalyticsFragment.DateFilter filter) {
        if (filter == AnalyticsFragment.DateFilter.ALL_TIME) {
            return history;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -filter.getHours());
        Date cutoff = cal.getTime();

        List<AquariumData> filtered = new ArrayList<>();
        for (AquariumData data : history) {
            if (data.getDate() != null && data.getDate().after(cutoff)) {
                filtered.add(data);
            }
        }
        return filtered;
    }
}
