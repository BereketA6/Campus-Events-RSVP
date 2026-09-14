package com.davidborsamo.campusevents.model;

/**
 * GOING     — confirmed attendee, counts against the event's capacity
 * WAITLISTED — event was full when they RSVP'd; automatically promoted to
 *              GOING if a confirmed attendee cancels (see RsvpService)
 */
public enum RsvpStatus {
    GOING,
    WAITLISTED
}
