# Campus Events — Event & RSVP Platform

A full-stack web app: students browse and RSVP to campus events (club meetings,
study groups, workshops); organizers create and manage events with capacity
limits and automatic waitlisting.


## What it does

- Students: browse/search/filter events by category, RSVP, get
  automatically waitlisted if an event is full, see "My Events"
- Organizers: everything a student can do, plus create/edit/delete their
  own events
- Capacity + waitlist logic: when someone cancels a confirmed RSVP, the
  longest-waiting waitlisted person is automatically promoted

## Stack

- Backend: Java 17, Spring Boot 3.3, Spring Security (JWT auth,
  role-based authorization), Spring Data JPA, H2 (file-based embedded
  database — no separate DB install needed)
- Frontend: Plain HTML/CSS/JS (same approach as the Practice Tracker —
  keeps focus on backend concepts)

## Setup

### Prerequisites
- JDK 17 or newer — check with `java -version`. If you don't have it:
  `brew install openjdk@17` (Mac) or download from adoptium.net.
- Maven — check with `mvn -version`. If missing: `brew install maven` (Mac).

### Run the backend

```bash
cd backend
mvn spring-boot:run
```

First run will take a while — Maven needs to download all the Spring Boot
dependencies (unlike Python/pip, this can be 100+ MB the first time). This
needs a real internet connection.

The API runs on http://localhost:8080. It auto-creates campus_events.mv.db
(H2 database file) in the backend folder on first run.

can browse the database directly at http://localhost:8080/h2-console
(JDBC URL: jdbc:h2:file:./campus_events, username sa, no password).

### Run the frontend

Just open frontend/index.html directly in browser.

## Project structure

```
campus-events/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/davidborsamo/campusevents/
│       │   ├── CampusEventsApplication.java   <- entry point
│       │   ├── model/          <- User, Event, Rsvp entities + enums
│       │   ├── repository/     <- Spring Data JPA interfaces
│       │   ├── security/       <- JWT generation/validation, auth filter
│       │   ├── config/         <- Spring Security configuration
│       │   ├── dto/            <- request/response objects (API contract)
│       │   ├── service/        <- business logic (auth, events, RSVPs)
│       │   ├── controller/     <- REST endpoints
│       │   └── exception/      <- custom exceptions + global error handler
│       └── test/java/.../service/RsvpServiceTest.java  <- unit tests
└── frontend/
    └── index.html              <- single-page UI
```

## API reference

| Method | Endpoint                | Auth              | Description                          |
|--------|--------------------------|-------------------|---------------------------------------|
| POST   | /api/auth/register       | No                | Create account (STUDENT or ORGANIZER) |
| POST   | /api/auth/login          | No                | Log in, returns a JWT                 |
| GET    | /api/events              | No                | Search/filter/paginate events         |
| GET    | /api/events/{id}         | No                | Get one event                         |
| POST   | /api/events              | ORGANIZER         | Create an event                       |
| PUT    | /api/events/{id}         | ORGANIZER (owner) | Update an event                       |
| DELETE | /api/events/{id}         | ORGANIZER (owner) | Delete an event                       |
| GET    | /api/events/mine         | ORGANIZER         | List events you organize              |
| POST   | /api/events/{id}/rsvp    | Any logged-in user| RSVP (GOING or WAITLISTED)            |
| DELETE | /api/events/{id}/rsvp    | Any logged-in user| Cancel your RSVP                      |
| GET    | /api/my-rsvps            | Any logged-in user| List events you've RSVP'd to          |
