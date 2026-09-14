package com.davidborsamo.campusevents.dto;

import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.EventCategory;

import java.time.LocalDateTime;

public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private LocalDateTime startTime;
    private Integer capacity;
    private String organizerUsername;
    private Long organizerId;

    // Derived fields — computed by the service layer, not stored directly.
    private long goingCount;
    private long waitlistCount;
    private boolean isFull;
    private String myRsvpStatus; // "GOING" | "WAITLISTED" | null if not logged in / no RSVP

    public static EventResponse from(Event event, long goingCount, long waitlistCount, String myRsvpStatus) {
        EventResponse dto = new EventResponse();
        dto.id = event.getId();
        dto.title = event.getTitle();
        dto.description = event.getDescription();
        dto.category = event.getCategory();
        dto.location = event.getLocation();
        dto.startTime = event.getStartTime();
        dto.capacity = event.getCapacity();
        dto.organizerUsername = event.getOrganizer().getUsername();
        dto.organizerId = event.getOrganizer().getId();
        dto.goingCount = goingCount;
        dto.waitlistCount = waitlistCount;
        dto.isFull = goingCount >= event.getCapacity();
        dto.myRsvpStatus = myRsvpStatus;
        return dto;
    }

    // --- Getters (no setters needed — this DTO is only ever built via the factory method above) ---

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventCategory getCategory() { return category; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public Integer getCapacity() { return capacity; }
    public String getOrganizerUsername() { return organizerUsername; }
    public Long getOrganizerId() { return organizerId; }
    public long getGoingCount() { return goingCount; }
    public long getWaitlistCount() { return waitlistCount; }
    public boolean isFull() { return isFull; }
    public String getMyRsvpStatus() { return myRsvpStatus; }
}
