package com.davidborsamo.campusevents.repository;

import com.davidborsamo.campusevents.model.Event;
import com.davidborsamo.campusevents.model.EventCategory;
import com.davidborsamo.campusevents.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByOrganizer(User organizer, Pageable pageable);

    // A single flexible search query: any parameter left null is ignored
    // (the "OR :param IS NULL" pattern). This avoids needing a separate
    // repository method for every combination of filters someone might want.
    @Query("""
        SELECT e FROM Event e
        WHERE (:category IS NULL OR e.category = :category)
        AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')))
        AND e.startTime >= :after
        ORDER BY e.startTime ASC
        """)
    Page<Event> search(
            @Param("category") EventCategory category,
            @Param("search") String search,
            @Param("after") LocalDateTime after,
            Pageable pageable
    );
}
