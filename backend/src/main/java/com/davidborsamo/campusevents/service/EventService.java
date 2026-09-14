package com.davidborsamo.campusevents.service;

import com.davidborsamo.campusevents.dto.EventRequest;
import com.davidborsamo.campusevents.dto.EventResponse;
import com.davidborsamo.campusevents.exception.ForbiddenActionException;
import com.davidborsamo.campusevents.exception.ResourceNotFoundException;
import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.EventCategory;
import com.davidborsamo.campusevents.model.Rsvp;
import com.davidborsamo.campusevents.model.User;
import com.davidborsamo.campusevents.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RsvpService rsvpService;

    public EventService(EventRepository eventRepository, RsvpService rsvpService) {
        this.eventRepository = eventRepository;
        this.rsvpService = rsvpService;
    }

    public Event createEvent(EventRequest request, User organizer) {
        Event event = new Event();
        applyRequestToEvent(request, event);
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    /**
     * Only the organizer who created an event may edit or delete it. This
     * check lives here in the service layer — not just hidden in the
     * frontend UI — because the frontend can always be bypassed by calling
     * the API directly. This is the same "authorization vs authentication"
     * distinction from the Flask project, just enforced with a different
     * mechanism (ownership check vs. a foreign-key filter on the query).
     */
    public Event updateEvent(Long eventId, EventRequest request, User requester) {
        Event event = getEventOrThrow(eventId);
        assertIsOwner(event, requester);
        applyRequestToEvent(request, event);
        return eventRepository.save(event);
    }

    public void deleteEvent(Long eventId, User requester) {
        Event event = getEventOrThrow(eventId);
        assertIsOwner(event, requester);
        eventRepository.delete(event);
    }

    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public Page<Event> searchEvents(EventCategory category, String search, boolean includePast, Pageable pageable) {
        LocalDateTime after = includePast ? LocalDateTime.of(2000, 1, 1, 0, 0) : LocalDateTime.now();
        return eventRepository.search(category, search, after, pageable);
    }

    public Page<Event> getEventsByOrganizer(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable);
    }

    /**
     * Builds the response DTO, including the derived RSVP fields. currentUser
     * is nullable — someone browsing without being logged in still sees
     * event listings (see SecurityConfig: GET /api/events is public), they
     * just won't see a "myRsvpStatus".
     */
    public EventResponse toResponse(Event event, User currentUser) {
        long going = rsvpService.countGoing(event);
        long waitlisted = rsvpService.countWaitlisted(event);

        String myStatus = null;
        if (currentUser != null) {
            Optional<Rsvp> rsvp = rsvpService.findRsvp(currentUser, event);
            myStatus = rsvp.map(r -> r.getStatus().name()).orElse(null);
        }

        return EventResponse.from(event, going, waitlisted, myStatus);
    }

    private void assertIsOwner(Event event, User requester) {
        if (!event.getOrganizer().getId().equals(requester.getId())) {
            throw new ForbiddenActionException("You can only modify events you organized");
        }
    }

    private void applyRequestToEvent(EventRequest request, Event event) {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setLocation(request.getLocation());
        event.setStartTime(request.getStartTime());
        event.setCapacity(request.getCapacity());
    }
}
