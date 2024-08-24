package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import ru.practicum.category.model.Category;
import ru.practicum.location.model.Location;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String annotation;

    @NonNull
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ColumnDefault("false")
    private Boolean paid;

    @Column(name = "participant_limit")
    @ColumnDefault("0")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    @ColumnDefault("true")
    private Boolean requestModeration;

    @NonNull
    private String title;

    private LocalDateTime created = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    private  EventState state;

}
