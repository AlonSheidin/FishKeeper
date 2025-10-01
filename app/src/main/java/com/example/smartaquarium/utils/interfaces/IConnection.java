package com.example.smartaquarium.utils.interfaces;

import com.example.smartaquarium.utils.enums.EnumConnectionStatus;

public interface IConnection {

    /**
         * Adds a listener to receive data updates.
         *
         * @param listener the listener to be added
         */
        void addListener(IDataListener listener);

        /**
         * Retrieves the current connection status.
         *
         * @return the connection status as an EnumConnectionStatus
         */
        EnumConnectionStatus getConnectionStatus();
}
