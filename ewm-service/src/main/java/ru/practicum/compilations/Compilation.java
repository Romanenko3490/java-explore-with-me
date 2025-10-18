package ru.practicum.compilations;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.events.Event;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compilations")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(name = "pinned", nullable = false)
    @Builder.Default
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
    private List<Event> events = new ArrayList<>();


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
