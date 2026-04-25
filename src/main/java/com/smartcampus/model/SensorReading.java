package com.smartcampus.model;

public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    // Default constructor (REQUIRED for JSON)
    public SensorReading() {
    }

    // Parameterized constructor
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getter and Setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter and Setter for value
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}