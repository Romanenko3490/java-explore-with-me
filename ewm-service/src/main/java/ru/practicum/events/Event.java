package ru.practicum.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import ru.practicum.categories.Category;
import ru.practicum.compilations.Compilation;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "events")
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String description;

    @Column(name = "event_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Embedded
    private Location location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(name = "request_moderation", nullable = false)
    @Builder.Default
    private Boolean requestModeration = true;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(name = "created_on", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @Column(name = "published_on")  // ← nullable = true (по умолчанию)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventState state = EventState.PENDING;

    @Column(name = "confirmed_requests", nullable = false)
    @Builder.Default
    private Integer confirmedRequests = 0;

    @Builder.Default
    private Long views = 0L;

    @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Compilation> compilations = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event that = (Event) o;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}