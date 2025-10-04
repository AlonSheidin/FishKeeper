package com.example.smartaquarium.data.model;

import java.time.LocalDateTime;
import java.util.Date;

public class AquariumData {
    public int temperature;
    public int ph;
    public int oxygen;
    public int waterLevel;
    public Date date; // Add this field


    public AquariumData(int temperature, int ph, int oxygen, int waterLevel) {
        this.temperature = temperature;
        this.ph = ph;
        this.oxygen = oxygen;
        this.waterLevel = waterLevel;
        this.date = new Date(); // Initialize with current time
    }
}
