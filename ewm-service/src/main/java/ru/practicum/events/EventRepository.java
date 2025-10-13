package ru.practicum.events;


import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategory_Id(Long categoryId);

    default List<Event> findByFilters(List<Long> users,
                                      List<EventState> states,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Pageable pageable) {

        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();

        if (users != null && !users.isEmpty()) {
            predicate.and(event.initiator.id.in(users));
        }

        if (states != null && !states.isEmpty()) {
            predicate.and(event.state.in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            predicate.and(event.category.id.in(categories));
        }

        if (rangeStart != null) {
            predicate.and(event.eventDate.goe(rangeStart));
        }

        if (rangeEnd != null) {
            predicate.and(event.eventDate.loe(rangeEnd));
        }

        return findAll(predicate, pageable).getContent();
    }
}
