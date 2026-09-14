package com.davidborsamo.campusevents.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * The RSVP is modeled as its own entity rather than a plain many-to-many
 * join table, because it needs extra fields (status, timestamp) beyond
 * just "this user is linked to this event." This is a common real-world
 * pattern: a many-to-many relationship with metadata always needs to
 * become its own entity rather than a bare join table.
 */
@Entity
@Table(name = "rsvps", uniqueConstraints = {
        // A user can only have one RSVP per event — enforced at the database
        // level, not just in application code, so it holds even under
        // concurrent requests.
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
public class Rsvp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Rsvp() {
    }

    public Rsvp(User user, Event event, RsvpStatus status) {
        this.user = user;
        this.event = event;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public RsvpStatus getStatus() { return status; }
    public void setStatus(RsvpStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
