package ru.practicum.comments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEvent_IdAndDeleted(Long eventId, boolean deleted, Pageable pageable);

    Page<Comment> findByAuthor_Id(Long authorId, Pageable pageable);

    Page<Comment> findByAuthor_IdAndDeleted(Long authorId, boolean deleted, Pageable pageable);


}
