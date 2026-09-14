package com.davidborsamo.campusevents.model;

/**
 * A user's role determines what they're authorized to do:
 * STUDENT   — browse events, RSVP, cancel their own RSVPs
 * ORGANIZER — everything a student can do, plus create/edit/delete events they own
 *
 * Spring Security checks this at the endpoint level (see SecurityConfig and
 * the @PreAuthorize annotations on EventController) — this is what
 * "role-based authorization" means in practice, not just checking a string
 * manually in every method.
 */
public enum Role {
    STUDENT,
    ORGANIZER
}
