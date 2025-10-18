package ru.practicum.events;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategory_Id(Long categoryId);

    default List<Event> findByAdminFilters(List<Long> users,
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

    List<Event> findByIdIn(Collection<Long> ids);


    default List<Event> findByPublicFilters(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            Boolean onlyAvailable,
                                            EventSortType sort,
                                            Integer from,
                                            Integer size
    ) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();


        predicate.and(event.state.eq(EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            String searchText = text.toLowerCase();
            BooleanBuilder textPredicate = new BooleanBuilder();
            textPredicate.or(event.annotation.toLowerCase().contains(searchText))
                    .or(event.title.toLowerCase().contains(searchText))
                    .or(event.description.toLowerCase().contains(searchText));
            predicate.and(textPredicate);
        }

        if (categories != null && !categories.isEmpty()) {
            predicate.and(event.category.id.in(categories));
        }

        if (paid != null) {
            predicate.and(event.paid.eq(paid));
        }

        if (rangeStart != null) {
            predicate.and(event.eventDate.goe(rangeStart));
        }

        if (rangeEnd != null) {
            predicate.and(event.eventDate.loe(rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            BooleanExpression availablePredicate = event.confirmedRequests.lt(event.participantLimit)
                    .or(event.participantLimit.eq(0));
            predicate.and(availablePredicate);
        }

        if (rangeStart == null) {
            predicate.and(event.eventDate.goe(LocalDateTime.now()));
        }

        if (sort == null) {
            sort = EventSortType.EVENT_DATE;
        }


        Sort sortObj = null;
        switch (sort) {
            case VIEWS -> sortObj = Sort.by(Sort.Direction.DESC, "views");
            case EVENT_DATE -> sortObj = Sort.by(Sort.Direction.ASC, "eventDate");
        }

        Pageable pageable = PageRequest.of(from / size, size, sortObj);

        return findAll(predicate, pageable).getContent();
    }
}
