package ru.practicum.requests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RequestsRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findByRequester_Id(Long requesterId);

    List<Request> findByEvent_Id(Long eventId);

    List<Request> findByIdIn(Collection<Long> ids);
}
