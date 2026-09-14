package com.davidborsamo.campusevents.controller;

import com.davidborsamo.campusevents.dto.EventRequest;
import com.davidborsamo.campusevents.dto.EventResponse;
import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.EventCategory;
import com.davidborsamo.campusevents.model.User;
import com.davidborsamo.campusevents.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Public — no login required to browse events (see SecurityConfig).
    // @AuthenticationPrincipal is null here if the caller isn't logged in,
    // which EventService.toResponse() handles gracefully.
    @GetMapping
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includePast,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.searchEvents(category, search, includePast, pageable);
        Page<EventResponse> response = events.map(e -> eventService.toResponse(e, currentUser));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Event event = eventService.getEventOrThrow(id);
        return ResponseEntity.ok(eventService.toResponse(event, currentUser));
    }

    // Only ORGANIZERs can create events. @PreAuthorize checks this BEFORE
    // the method body runs — Spring Security intercepts the call entirely
    // if the role doesn't match, returning 403 automatically.
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Event event = eventService.createEvent(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.toResponse(event, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Event event = eventService.updateEvent(id, request, currentUser);
        return ResponseEntity.ok(eventService.toResponse(event, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        eventService.deleteEvent(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<EventResponse>> getMyEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.getEventsByOrganizer(currentUser, pageable);
        Page<EventResponse> response = events.map(e -> eventService.toResponse(e, currentUser));
        return ResponseEntity.ok(response);
    }
}
