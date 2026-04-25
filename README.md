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
git clone https://github.com/SandeepaSilva/SmartCampusAPI-CSA.git
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

### 6. Delete a Room with No Sensors (succeeds)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/EMPTY-101
```

### 7. Delete a Room with Sensors (returns 409 Conflict)
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

## Conceptual Report — Questions & Answers

### Part 1: Service Architecture & Setup

**Q1: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton? How does this affect in-memory data management?**

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request. This is known as the per-request lifecycle. Each request gets its own fresh object, which means instance variables on the resource class are not shared between requests and cannot be used to store persistent state.

This design decision has a significant impact on how in-memory data structures must be managed. Since each resource instance is discarded after the request completes, any data stored as an instance variable would be lost. To maintain persistent in-memory state across requests, data structures such as HashMap or ArrayList must be stored in a shared, static or application-scoped location — for example, a singleton DataStore class with static fields.

Additionally, because multiple requests can arrive concurrently and access the same shared data, synchronization must be considered. Using thread-safe collections such as ConcurrentHashMap, or using synchronized blocks, prevents race conditions and data corruption when multiple threads read or write simultaneously.

---

**Q2: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?**

HATEOAS (Hypermedia as the Engine of Application State) is a constraint of REST architecture where API responses include links to related resources and available actions, rather than requiring clients to construct URLs manually.

This is considered an advanced RESTful design principle because it makes the API self-describing and discoverable. A client does not need to know the full URL structure in advance — it simply follows the links provided in each response, much like a user navigating a website by clicking hyperlinks.

For client developers, this approach offers several benefits. It reduces tight coupling between the client and the server, meaning the server can change its URL structure without breaking existing clients as long as the link relationships remain consistent. It also reduces the need for clients to rely entirely on static external documentation, since the API itself guides the client through available operations at each step of an interaction.

---

### Part 2: Room Management

**Q3: When returning a list of rooms, what are the implications of returning only IDs versus returning full room objects?**

Returning only IDs in a list response minimises the size of the payload, which reduces network bandwidth usage. However, it forces the client to make additional HTTP requests for each ID to retrieve the full details of each room, resulting in what is commonly called the N+1 request problem. This increases latency and places more load on both the client and server.

Returning full room objects in the list response increases the payload size but provides the client with all the information it needs in a single request, reducing round trips and improving performance for most use cases.

The optimal approach depends on the context. If clients typically need full details — as is common in dashboard or management interfaces — returning full objects is preferable. If clients only need a summary or will selectively fetch details, returning IDs with a link to the full resource (following HATEOAS principles) may be more efficient.

---

**Q4: Is the DELETE operation idempotent in your implementation? Justify your answer.**

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once.

In this API, the first DELETE request for a room that exists and has no sensors will successfully remove the room and return a 200 OK response. If the client mistakenly sends the exact same DELETE request a second time, the room no longer exists in the system, so the API returns a 404 Not Found response.

Although the HTTP status codes differ between the first and subsequent requests, the state of the server remains the same — the room is absent in all cases. This satisfies the definition of idempotency, which is about the server state rather than the response code. The resource is deleted and stays deleted regardless of how many times the request is repeated.

---

### Part 3: Sensor Operations & Linking

**Q5: What happens if a client sends data in a format other than JSON to a POST endpoint annotated with @Consumes(MediaType.APPLICATION_JSON)?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the endpoint only accepts requests with a Content-Type of `application/json`. If a client sends a request with a different Content-Type, such as `text/plain` or `application/xml`, JAX-RS will reject the request before it even reaches the resource method.

The framework will automatically return an HTTP 415 Unsupported Media Type response, indicating that the server cannot process the request body in the format provided. The resource method is never invoked.

This behaviour is handled entirely by the JAX-RS runtime through content negotiation. It protects the server from receiving unexpected data formats and ensures that the message body reader for JSON (provided by Jackson via jersey-media-json-jackson) is only applied when the content type is appropriate.

---

**Q6: Why is using @QueryParam for filtering generally considered superior to embedding the filter in the URL path?**

Query parameters are semantically designed for filtering, searching, and sorting a collection of resources, whereas URL path segments are intended to identify specific resources. Using `/api/v1/sensors?type=CO2` correctly conveys that the client is requesting the sensors collection with a filter applied, while `/api/v1/sensors/type/CO2` implies that `type/CO2` is a specific nested resource, which is misleading and semantically incorrect.

Query parameters are also optional by nature, making it straightforward to support unfiltered requests (`GET /sensors`) and filtered requests (`GET /sensors?type=CO2`) on the same endpoint without requiring separate methods. Multiple filters can also be combined easily using additional query parameters (e.g., `?type=CO2&status=ACTIVE`), whereas path-based filtering becomes increasingly complex and unreadable as more filter criteria are added.

Additionally, query parameters are a well-established convention understood by HTTP clients, proxies, and caching mechanisms, making the API more intuitive for developers.

---

### Part 4: Deep Nesting with Sub-Resources

**Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern in JAX-RS.**

The Sub-Resource Locator pattern allows a JAX-RS resource class to delegate the handling of a sub-path to a separate, dedicated resource class. In this API, the `SensorResource` class contains a locator method for the path `{sensorId}/readings`, which returns an instance of `SensorReadingResource`. JAX-RS then delegates all further path matching and HTTP method handling to that class.

The primary architectural benefit is separation of concerns. Each resource class has a single, well-defined responsibility — `SensorResource` manages sensors, and `SensorReadingResource` manages readings. This makes the codebase easier to read, maintain, and test independently.

In large APIs with many nested resources, placing every endpoint in a single controller class would quickly become unmanageable. The Sub-Resource Locator pattern promotes modularity by breaking the API into smaller, focused classes. It also makes it easier to extend the API — new sub-resources can be added by creating new classes and registering locators without modifying existing resource classes.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q8: Why is HTTP 422 Unprocessable Entity often more semantically accurate than 404 Not Found when a POST body references a non-existent resource?**

A 404 Not Found response indicates that the requested URL or resource could not be found on the server. In the context of a POST request to `/api/v1/sensors`, the endpoint itself exists and is reachable — so returning 404 would be misleading and technically incorrect.

The actual problem is not that the endpoint is missing, but that the JSON payload contains a `roomId` that references a room which does not exist in the system. The request is syntactically valid JSON and is directed at a valid endpoint, but it cannot be processed because of a semantic error within the payload.

HTTP 422 Unprocessable Entity is designed precisely for this situation — it signals that the server understands the request format and the endpoint is valid, but the content of the request body contains a logical or referential error that prevents processing. This gives the client a much clearer and more actionable error signal compared to 404.

---

**Q9: From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?**

Exposing raw Java stack traces in API error responses is a significant security risk for several reasons.

First, stack traces reveal the internal structure of the application, including class names, package names, method names, and line numbers. An attacker can use this information to map the application's architecture and identify specific frameworks, libraries, and versions in use.

Second, if a known vulnerability exists in any of those libraries or frameworks, the attacker now has the information needed to craft a targeted exploit. For example, knowing that the application uses a specific version of a library with a published CVE gives an attacker a clear attack vector.

Third, stack traces can expose file system paths, database query fragments, or configuration details that further assist in reconnaissance.

The global `ExceptionMapper<Throwable>` addresses this by intercepting all unexpected exceptions and returning only a generic HTTP 500 response with a safe, non-revealing error message, ensuring that internal implementation details are never disclosed to external clients.

---

**Q10: Why is it better to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() calls in every resource method?**

Using JAX-RS filters for logging follows the principle of separation of concerns and avoids code duplication. If logging statements are manually inserted into every resource method, the same boilerplate code must be written and maintained across dozens of methods. Any change to the logging format or behaviour requires updating every single method individually, which is error-prone and time-consuming.

A `ContainerRequestFilter` and `ContainerResponseFilter`, registered once with the JAX-RS runtime, are automatically applied to every incoming request and outgoing response without any modification to the resource classes themselves. This makes the logging logic centralised, consistent, and easy to maintain.

Filters also execute at the framework level, meaning they capture requests and responses even when exceptions are thrown and handled by exception mappers. Manual logging inside resource methods would miss these cases. Furthermore, using filters keeps resource classes focused purely on business logic, improving readability and testability.

---

## Author

**Sandeep Silva**
**University of Westminster**
**Module: 5COSC022W Client-Server Architectures**

