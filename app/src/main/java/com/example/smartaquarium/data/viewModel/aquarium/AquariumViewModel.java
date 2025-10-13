package com.example.smartaquarium.data.viewModel.aquarium;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.smartaquarium.R;
import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.data.viewModel.aquariumData.AquariumDataViewModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel for the AquariumFragment.
 * This class transforms the latest AquariumData point into individual, styled BarData objects
 * for each metric to be displayed on the overview dashboard.
 */
public class AquariumViewModel extends AndroidViewModel {

    // --- Output LiveData ---
    // The fragment will observe these to get the final, styled chart data.
    public final LiveData<BarData> temperatureBarData;
    public final LiveData<BarData> phBarData;
    public final LiveData<BarData> oxygenBarData;
    public final LiveData<BarData> waterLevelBarData;
    public final LiveData<String> lastUpdatedTimestamp;

    public AquariumViewModel(@NonNull Application application, @NonNull ViewModelStoreOwner owner) {
        super(application);

        // Get the single, shared instance of the ViewModel that provides the raw data.
        AquariumDataViewModel dataProviderViewModel = new ViewModelProvider(owner).get(AquariumDataViewModel.class);

        // Initialize all the data streams and transformations.
        // This keeps the constructor clean.
        lastUpdatedTimestamp = initializeTimestampStream(dataProviderViewModel);
        temperatureBarData = initializeTemperatureStream(dataProviderViewModel);
        phBarData = initializePhStream(dataProviderViewModel);
        oxygenBarData = initializeOxygenStream(dataProviderViewModel);
        waterLevelBarData = initializeWaterLevelStream(dataProviderViewModel);
    }

    // --- Initialization Methods ---

    private LiveData<String> initializeTimestampStream(AquariumDataViewModel dataProvider) {
        // The "trigger" is the LiveData that emits the most recent data point.
        LiveData<AquariumData> latestDataStream = dataProvider.getLatestData();

        // Transform the timestamp into a readable string.
        return Transformations.map(latestDataStream, latestAquariumData -> {
            if (latestAquariumData != null && latestAquariumData.getDate() != null) {
                // Get the Date object directly.
                Date dateObject = latestAquariumData.getDate();

                // Format the Date object into a readable time string.
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                return getApplication().getString(R.string.last_updated_empty) + sdf.format(dateObject);
            }
            return getApplication().getString(R.string.last_updated_empty);
        });
    }

    private LiveData<BarData> initializeTemperatureStream(AquariumDataViewModel dataProvider) {
        return Transformations.map(dataProvider.getLatestData(), data -> {
            if (data == null) return null;
            return createSingleBarData(data.getTemperature(), "Temp °C", R.color.chart_temperature);
        });
    }

    private LiveData<BarData> initializePhStream(AquariumDataViewModel dataProvider) {
        return Transformations.map(dataProvider.getLatestData(), data -> {
            if (data == null) return null;
            return createSingleBarData(data.getPh(), "pH", R.color.chart_ph);
        });
    }

    private LiveData<BarData> initializeOxygenStream(AquariumDataViewModel dataProvider) {
        return Transformations.map(dataProvider.getLatestData(), data -> {
            if (data == null) return null;
            return createSingleBarData(data.getOxygen(), "Oxygen %", R.color.chart_oxygen);
        });
    }

    private LiveData<BarData> initializeWaterLevelStream(AquariumDataViewModel dataProvider) {
        return Transformations.map(dataProvider.getLatestData(), data -> {
            if (data == null) return null;
            return createSingleBarData(data.getWaterLevel(), "Level %", R.color.chart_water_level);
        });
    }


    /**
     * A helper function to create a styled BarData object for a single value.
     * @param value The integer value to display.
     * @param label The label for the data set.
     * @param colorResId The color resource ID for the bar.
     * @return A fully formed BarData object.
     */
    private BarData createSingleBarData(int value, String label, int colorResId) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, value));

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(ContextCompat.getColor(getApplication(), colorResId));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);

        return new BarData(dataSet);
    }
}
