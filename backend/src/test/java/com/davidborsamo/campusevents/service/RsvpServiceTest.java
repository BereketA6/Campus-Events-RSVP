package com.davidborsamo.campusevents.service;

import com.davidborsamo.campusevents.exception.ConflictException;
import com.davidborsamo.campusevents.model.*;
import com.davidborsamo.campusevents.repository.EventRepository;
import com.davidborsamo.campusevents.repository.RsvpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the core business rule of the app: capacity enforcement and
 * waitlist promotion. These are unit tests — the repositories are mocked,
 * not hitting a real database — so they run fast and test RsvpService's
 * logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class RsvpServiceTest {

    @Mock
    private RsvpRepository rsvpRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RsvpService rsvpService;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "hashed", Role.STUDENT);
        user.setId(1L);

        event = new Event();
        event.setId(1L);
        event.setCapacity(2);
    }

    @Test
    void joinEvent_belowCapacity_setsStatusGoing() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(rsvpRepository.countByEventAndStatus(event, RsvpStatus.GOING)).thenReturn(0L);
        when(rsvpRepository.save(any(Rsvp.class))).thenAnswer(inv -> inv.getArgument(0));

        Rsvp result = rsvpService.joinEvent(user, 1L);

        assertEquals(RsvpStatus.GOING, result.getStatus());
    }

    @Test
    void joinEvent_atCapacity_setsStatusWaitlisted() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        // Event capacity is 2, and 2 people are already GOING — the event is full.
        when(rsvpRepository.countByEventAndStatus(event, RsvpStatus.GOING)).thenReturn(2L);
        when(rsvpRepository.save(any(Rsvp.class))).thenAnswer(inv -> inv.getArgument(0));

        Rsvp result = rsvpService.joinEvent(user, 1L);

        assertEquals(RsvpStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void joinEvent_alreadyRsvpd_throwsConflictException() {
        Rsvp existingRsvp = new Rsvp(user, event, RsvpStatus.GOING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(existingRsvp));

        assertThrows(ConflictException.class, () -> rsvpService.joinEvent(user, 1L));

        // Confirm we never even check capacity or attempt to save a duplicate —
        // the method should short-circuit as soon as the duplicate is found.
        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void cancelRsvp_wasGoing_promotesOldestWaitlistedPerson() {
        User waitlistedUser = new User("waiting_user", "wait@example.com", "hashed", Role.STUDENT);
        waitlistedUser.setId(2L);

        Rsvp cancelingRsvp = new Rsvp(user, event, RsvpStatus.GOING);
        Rsvp waitlistedRsvp = new Rsvp(waitlistedUser, event, RsvpStatus.WAITLISTED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(cancelingRsvp));
        when(rsvpRepository.findFirstByEventAndStatusOrderByCreatedAtAsc(event, RsvpStatus.WAITLISTED))
                .thenReturn(Optional.of(waitlistedRsvp));

        rsvpService.cancelRsvp(user, 1L);

        // The canceled RSVP should be deleted...
        verify(rsvpRepository).delete(cancelingRsvp);
        // ...and the waitlisted person should be promoted to GOING and saved.
        assertEquals(RsvpStatus.GOING, waitlistedRsvp.getStatus());
        verify(rsvpRepository).save(waitlistedRsvp);
    }

    @Test
    void cancelRsvp_wasWaitlisted_doesNotPromoteAnyone() {
        Rsvp cancelingRsvp = new Rsvp(user, event, RsvpStatus.WAITLISTED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(cancelingRsvp));

        rsvpService.cancelRsvp(user, 1L);

        verify(rsvpRepository).delete(cancelingRsvp);
        // Canceling a waitlist spot doesn't free up a GOING slot, so no
        // promotion lookup should even happen.
        verify(rsvpRepository, never()).findFirstByEventAndStatusOrderByCreatedAtAsc(any(), any());
    }
}
