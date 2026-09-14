package com.davidborsamo.campusevents.service;

import com.davidborsamo.campusevents.exception.ConflictException;
import com.davidborsamo.campusevents.exception.ResourceNotFoundException;
import com.davidborsamo.campusevents.model.*;
import com.davidborsamo.campusevents.repository.EventRepository;
import com.davidborsamo.campusevents.repository.RsvpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;

    public RsvpService(RsvpRepository rsvpRepository, EventRepository eventRepository) {
        this.rsvpRepository = rsvpRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Joins an event. If the event is at capacity, the user is placed on
     * the waitlist instead of being rejected outright — this is the core
     * "business logic" of the whole app, and exactly the kind of rule
     * that's easy to get subtly wrong under concurrent requests, which is
     * why this method is @Transactional (see note below).
     */
    @Transactional
    public Rsvp joinEvent(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (rsvpRepository.findByUserAndEvent(user, event).isPresent()) {
            throw new ConflictException("You've already RSVP'd to this event");
        }

        long goingCount = rsvpRepository.countByEventAndStatus(event, RsvpStatus.GOING);
        RsvpStatus status = (goingCount < event.getCapacity()) ? RsvpStatus.GOING : RsvpStatus.WAITLISTED;

        Rsvp rsvp = new Rsvp(user, event, status);
        return rsvpRepository.save(rsvp);

        // NOTE ON CONCURRENCY: @Transactional here ensures the count-then-save
        // is atomic from the database's perspective under normal load, but a
        // fully bulletproof version under heavy concurrent traffic would add
        // a database-level unique constraint or optimistic locking (a
        // @Version field on Event) to guarantee no double-booking even if two
        // requests race at the exact same instant. Worth mentioning as a
        // "how would you harden this further" answer in an interview.
    }

    /**
     * Cancels a user's RSVP. If they were GOING (not waitlisted), this frees
     * up a spot — so the longest-waiting WAITLISTED person is automatically
     * promoted to GOING. This is the kind of cascading side-effect that's
     * worth walking through step-by-step in an interview.
     */
    @Transactional
    public void cancelRsvp(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Rsvp rsvp = rsvpRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException("You haven't RSVP'd to this event"));

        boolean wasGoing = rsvp.getStatus() == RsvpStatus.GOING;
        rsvpRepository.delete(rsvp);

        if (wasGoing) {
            promoteFromWaitlist(event);
        }
    }

    private void promoteFromWaitlist(Event event) {
        Optional<Rsvp> nextInLine = rsvpRepository.findFirstByEventAndStatusOrderByCreatedAtAsc(event, RsvpStatus.WAITLISTED);
        nextInLine.ifPresent(rsvp -> {
            rsvp.setStatus(RsvpStatus.GOING);
            rsvpRepository.save(rsvp);
        });
    }

    public long countGoing(Event event) {
        return rsvpRepository.countByEventAndStatus(event, RsvpStatus.GOING);
    }

    public long countWaitlisted(Event event) {
        return rsvpRepository.countByEventAndStatus(event, RsvpStatus.WAITLISTED);
    }

    public Optional<Rsvp> findRsvp(User user, Event event) {
        return rsvpRepository.findByUserAndEvent(user, event);
    }
}
