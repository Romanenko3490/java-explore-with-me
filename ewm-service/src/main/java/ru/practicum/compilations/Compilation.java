package ru.practicum.compilations;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.events.Event;
import ru.practicum.events.EventState;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "compilations")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;

    @Column(name = "title", nullable = false, length = 255, unique = true)
    private String title;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @Builder.Default
    private Set<Event> events = new HashSet<>();


    public void addEvent(Event event) {
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public boolean containsEvent(Event event) {
        return events.contains(event);
    }
}
