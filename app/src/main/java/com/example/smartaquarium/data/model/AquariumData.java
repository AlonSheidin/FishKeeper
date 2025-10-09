package com.example.smartaquarium.data.model;


import java.util.Date;

public class AquariumData {
    public int temperature;
    public int ph;
    public int oxygen;
    public int waterLevel;
    public Date date;

    // No‑argument constructor required by Firestore
    public AquariumData() {}

    // Constructor for convenience
    public AquariumData(int temperature, int ph, int oxygen, int waterLevel) {
        this.temperature = temperature;
        this.ph = ph;
        this.oxygen = oxygen;
        this.waterLevel = waterLevel;
        this.date = new Date(); // Initialize with current time
    }

    public int getTemperature() { return temperature; }
    public int getPh() { return ph; }
    public int getOxygen() { return oxygen; }
    public int getWaterLevel() { return waterLevel; }
    public Date getDate() { return date; }
}
