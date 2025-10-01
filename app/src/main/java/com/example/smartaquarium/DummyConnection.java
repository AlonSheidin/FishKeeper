package com.example.smartaquarium;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class DummyConnection implements IConnection {

    private final ArrayList<IDataListener> listeners = new ArrayList<>();
    private Random random;
    private HandlerThread handlerThread;
    private Handler bgHandler;
    private Handler uiHandler;

    /**
     * Initializes the background thread and handlers for managing background and UI tasks,
     * as well as the random number generator used for generating aquarium data.
     */
    private void Init() {
        random = new Random();
        handlerThread = new HandlerThread("DummyConnectionThread");
        handlerThread.start(); // Start the HandlerThread before accessing its Looper
        bgHandler = new Handler(handlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Constructor for the DummyConnection class.
     * This initializes the necessary components, starts the background thread,
     * and schedules the data task to run on the background handler.
     */
    public DummyConnection() {
        Init();
        bgHandler.post(dataTask); // Start the data task
    }



    /**
     * Adds a listener to the list of listeners that will be notified of new data updates.
     *
     * @param listener An implementation of the IDataListener interface that will receive updates.
     */
    public void addListener(IDataListener listener) {
        listeners.add(listener);
    }


    /**
     * A Runnable task that generates random aquarium data and notifies all registered listeners.
     * The task is scheduled to run repeatedly every 60 seconds on a background thread.
     */
    Runnable dataTask = new Runnable() {
        @Override
        public void run() {
            // Generate random aquarium data
            AquariumData data = new AquariumData(
                    random.nextInt(30) + 15, // Temperature between 15 and 45
                    random.nextInt(100),    // pH between 0 and 100
                    random.nextInt(100));   // Oxygen between 0 and 100

            // Notify all registered listeners with the generated data
            notifyListeners(data);

            // Schedule the next execution of this task after 60 seconds
            bgHandler.postDelayed(this, 6000);
        }
    };

    /**
     * Notifies all registered listeners with the provided aquarium data.
     * This method ensures that the listeners are updated on the main UI thread.
     *
     * @param data The `AquariumData` object containing the updated data to be sent to the listeners.
     */
    private void notifyListeners(AquariumData data) {
        // Update UI on main thread
        for (IDataListener listener : listeners) {
            Log.println(Log.INFO, "DummyConnection", "Notifying listener ("+listener.getClass().getName()+"): " +"temperature="+ data.temperature + ", ph=" + data.ph + ", oxygen=" + data.oxygen);
            uiHandler.post(() -> listener.onNewData(data));
        }
    }
}
