package com.example.smartaquarium.data.model;

/**
 * Data model representing an individual Aquarium.
 */
public class Aquarium {
    private String id;
    private String name;

    // Required empty constructor for Firestore toObject()
    public Aquarium() {}

    public Aquarium(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}