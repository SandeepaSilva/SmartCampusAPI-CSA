package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    public static Map<String, Room> rooms = new HashMap<>();
    public static Map<String, Sensor> sensors = new HashMap<>();
    public static Map<String, List<SensorReading>> readings = new HashMap<>();

    static {
        // Optional sample room
        Room room = new Room();
        room.setId("LIB-301");
        room.setName("Library Quiet Study");
        room.setCapacity(50);

        rooms.put(room.getId(), room);

        readings.put("TEMP-001", new ArrayList<>());
    }
}