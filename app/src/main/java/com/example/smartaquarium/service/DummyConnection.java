package com.example.smartaquarium.service;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.example.smartaquarium.data.model.AquariumData;
import com.example.smartaquarium.utils.enums.EnumConnectionStatus;
import com.example.smartaquarium.utils.interfaces.IConnection;
import com.example.smartaquarium.utils.interfaces.IDataListener;

import java.util.ArrayList;
import java.util.Random;

public class DummyConnection implements IConnection {


    private final ArrayList<IDataListener> listeners = new ArrayList<>();
    private EnumConnectionStatus connectionStatus;


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
        connectionStatus = EnumConnectionStatus.CONNECTED;
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
     * This method is used to register an implementation of the `IDataListener` interface,
     * which will receive updates whenever new aquarium data is generated.
     *
     * @param listener An implementation of the `IDataListener` interface that will receive updates.
     *                 The listener is added to the internal list of listeners.
     */
    public void addListener(IDataListener listener) {
        listeners.add(listener);
    }

    /**
     * Retrieves the current connection status of the `DummyConnection`.
     * The status indicates whether the connection is active, disconnected, or in another state
     * as defined by the `EnumConnectionStatus` enumeration.
     * DummyConnection always returns `CONNECTED` as it simulates a stable connection.
     *
     * @return The current connection status as an `EnumConnectionStatus` value.
     */
    public EnumConnectionStatus getConnectionStatus() {
        return connectionStatus;
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
                    random.nextInt(100),   // Oxygen between 0 and 100
                    random.nextInt(100) // Water level between 0 and 100

            );

            // Notify all registered listeners with the generated data
            notifyListeners(data);

            // Schedule the next execution of this task after 30 seconds
            bgHandler.postDelayed(this, 5000);
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
            Log.println(Log.INFO, "DummyConnection", "Notifying listener ("+listener.getClass().getName()+"): date="+data.date.getTime() +" temperature="+ data.temperature + ", ph=" + data.ph + ", oxygen=" + data.oxygen);

            uiHandler.post(() -> {
                listener.onNewData(data);
                listener.onConnectionStatusChanged(connectionStatus);
            });

        }
    }
}
