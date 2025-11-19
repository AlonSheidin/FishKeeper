package com.example.smartaquarium.data.model;

/**
 * A POJO that represents a user's customizable settings for alerts and notifications.
 * This structure is used by Firestore for automatic data mapping.
 */
public class UserSettings {

    // Temperature thresholds
    private double minTemperature;
    private double maxTemperature;

    // pH thresholds
    private double minPh;
    private double maxPh;

    // Oxygen thresholds
    private double minOxygen;
    private double maxOxygen;

    // Water Level thresholds
    private double minWaterLevel;
    private double maxWaterLevel;

    // Do Not Disturb hours
    private int doNotDisturbStartHour;
    private int doNotDisturbEndHour;

    /**
     * IMPORTANT: A public, no-argument constructor is required by Firestore.
     * We set sensible defaults here for a typical freshwater aquarium.
     */
    public UserSettings() {
        // Temperature defaults
        this.minTemperature = 22.0;
        this.maxTemperature = 28.0;

        // pH defaults
        this.minPh = 6.5;
        this.maxPh = 7.5;

        // Oxygen defaults (mg/L)
        this.minOxygen = 5.0;
        this.maxOxygen = 12.0;

        // Water Level defaults (%)
        this.minWaterLevel = 80.0;
        this.maxWaterLevel = 100.0;

        // DND defaults
        this.doNotDisturbStartHour = 22; // 10 PM
        this.doNotDisturbEndHour = 7;    // 7 AM
    }

    // --- Getters and Setters ---
    // All of these are required by Firestore for data mapping.

    public double getMinTemperature() { return minTemperature; }
    public void setMinTemperature(double minTemperature) { this.minTemperature = minTemperature; }

    public double getMaxTemperature() { return maxTemperature; }
    public void setMaxTemperature(double maxTemperature) { this.maxTemperature = maxTemperature; }

    public double getMinPh() { return minPh; }
    public void setMinPh(double minPh) { this.minPh = minPh; }

    public double getMaxPh() { return maxPh; }
    public void setMaxPh(double maxPh) { this.maxPh = maxPh; }

    public double getMinOxygen() { return minOxygen; }
    public void setMinOxygen(double minOxygen) { this.minOxygen = minOxygen; }

    public double getMaxOxygen() { return maxOxygen; }
    public void setMaxOxygen(double maxOxygen) { this.maxOxygen = maxOxygen; }

    public double getMinWaterLevel() { return minWaterLevel; }
    public void setMinWaterLevel(double minWaterLevel) { this.minWaterLevel = minWaterLevel; }

    public double getMaxWaterLevel() { return maxWaterLevel; }
    public void setMaxWaterLevel(double maxWaterLevel) { this.maxWaterLevel = maxWaterLevel; }

    public int getDoNotDisturbStartHour() { return doNotDisturbStartHour; }
    public void setDoNotDisturbStartHour(int doNotDisturbStartHour) { this.doNotDisturbStartHour = doNotDisturbStartHour; }

    public int getDoNotDisturbEndHour() { return doNotDisturbEndHour; }
    public void setDoNotDisturbEndHour(int doNotDisturbEndHour) { this.doNotDisturbEndHour = doNotDisturbEndHour; }
}
