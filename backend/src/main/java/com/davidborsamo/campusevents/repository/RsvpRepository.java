package com.davidborsamo.campusevents.repository;

import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.Rsvp;
import com.davidborsamo.campusevents.model.RsvpStatus;
import com.davidborsamo.campusevents.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<Rsvp, Long> {
    Optional<Rsvp> findByUserAndEvent(User user, Event event);
    List<Rsvp> findByUser(User user);
    List<Rsvp> findByEventAndStatus(Event event, RsvpStatus status);
    long countByEventAndStatus(Event event, RsvpStatus status);

    // Used to find the next person to promote off the waitlist when a spot
    // opens up — oldest waitlisted RSVP first (first come, first served).
    Optional<Rsvp> findFirstByEventAndStatusOrderByCreatedAtAsc(Event event, RsvpStatus status);
}
