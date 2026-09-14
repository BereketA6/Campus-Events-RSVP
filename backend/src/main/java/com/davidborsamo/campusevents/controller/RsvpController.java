package com.davidborsamo.campusevents.controller;

import com.davidborsamo.campusevents.dto.EventResponse;
import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.Rsvp;
import com.davidborsamo.campusevents.model.User;
import com.davidborsamo.campusevents.repository.RsvpRepository;
import com.davidborsamo.campusevents.service.EventService;
import com.davidborsamo.campusevents.service.RsvpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RsvpController {

    private final RsvpService rsvpService;
    private final EventService eventService;
    private final RsvpRepository rsvpRepository;

    public RsvpController(RsvpService rsvpService, EventService eventService, RsvpRepository rsvpRepository) {
        this.rsvpService = rsvpService;
        this.eventService = eventService;
        this.rsvpRepository = rsvpRepository;
    }

    @PostMapping("/events/{eventId}/rsvp")
    public ResponseEntity<Map<String, String>> joinEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        Rsvp rsvp = rsvpService.joinEvent(currentUser, eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", rsvp.getStatus().name()));
    }

    @DeleteMapping("/events/{eventId}/rsvp")
    public ResponseEntity<Void> cancelRsvp(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        rsvpService.cancelRsvp(currentUser, eventId);
        return ResponseEntity.noContent().build();
    }

    // "My events" — everything the logged-in user has RSVP'd to (going or waitlisted)
    @GetMapping("/my-rsvps")
    public ResponseEntity<List<EventResponse>> getMyRsvps(@AuthenticationPrincipal User currentUser) {
        List<Rsvp> rsvps = rsvpRepository.findByUser(currentUser);
        List<EventResponse> events = rsvps.stream()
                .map(rsvp -> eventService.toResponse(rsvp.getEvent(), currentUser))
                .toList();
        return ResponseEntity.ok(events);
    }
}
