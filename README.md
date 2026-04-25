# SmartCampusAPI

A RESTful API built with JAX-RS (Jersey 3.1.5) and deployed on Apache Tomcat 10, designed to manage Rooms and Sensors across a university Smart Campus infrastructure.

---

## API Overview

This API provides endpoints to manage:
- **Rooms** – Physical spaces on campus
- **Sensors** – Devices deployed within rooms (Temperature, CO2, Occupancy, etc.)
- **Sensor Readings** – Historical measurement logs for each sensor

The API follows RESTful principles including proper HTTP status codes, JSON responses, resource nesting, and structured error handling.

**Base URL:** `http://localhost:8080/SmartCampusAPI/api/v1`

---

## Technology Stack

- **Language:** Java 17
- **Framework:** JAX-RS with Jersey 3.1.5
- **Server:** Apache Tomcat 10
- **Data Storage:** In-memory (HashMap / ArrayList)
- **Build Tool:** Maven
- **JSON Support:** Jackson (jersey-media-json-jackson)

---

## Project Structure

```
SmartCampusAPI/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/smartcampus/
│       │       ├── exception/        # Custom exceptions
│       │       ├── filter/           # Logging filters
│       │       ├── mapper/           # Exception mappers
│       │       ├── model/            # POJOs (Room, Sensor, SensorReading)
│       │       ├── resource/         # JAX-RS Resource classes
│       │       └── store/            # In-memory data store
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml
├── pom.xml
└── README.md
```

---

## How to Build and Run

### Prerequisites
- Java 17 or higher
- Apache Maven
- Apache Tomcat 10
- NetBeans IDE (recommended)

### Steps

1. **Clone the repository:**
```bash
git clone https://github.com/SandeepaSilva/SandeepaSilva-SmartCampusAPI-CSA.git
```

2. **Open in NetBeans:**
   - File → Open Project
   - Select the cloned folder
   - Click Open

3. **Build the project:**
```bash
mvn clean install
```

4. **Deploy to Tomcat:**
   - Click the Run button in NetBeans
   - Tomcat will start automatically
   - Server will be available at `http://localhost:8080/SmartCampusAPI/`

---

## API Endpoints

### Discovery
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Returns API metadata and resource links |

### Rooms
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/rooms` | Get all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (fails if sensors assigned) |

### Sensors
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/sensors` | Get all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |

### Sensor Readings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading for a sensor |

---

## Sample curl Commands

### 1. Get API Discovery Info
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"LIB-301\", \"name\": \"Library Quiet Study\", \"capacity\": 50}"
```

### 3. Create a Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-001\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 22.5, \"roomId\": \"LIB-301\"}"
```

### 4. Get All Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"READ-001\", \"timestamp\": 1714000000000, \"value\": 23.1}"
```

### 6. Delete a Room (with no sensors - succeeds)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/EMPTY-101
```

### 7. Delete a Room (with sensors - returns 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

---

## Error Handling

| HTTP Status | Scenario |
|-------------|----------|
| 409 Conflict | Attempting to delete a room that still has sensors |
| 422 Unprocessable Entity | Creating a sensor with a non-existent roomId |
| 403 Forbidden | Posting a reading to a sensor with MAINTENANCE status |
| 500 Internal Server Error | Any unexpected runtime error |

---

## Author

**Sandeep Silva**  
University of Westminster  
Module: 5COSC022W Client-Server Architectures