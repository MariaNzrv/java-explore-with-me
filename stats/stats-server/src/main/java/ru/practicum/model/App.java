package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;


@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "apps")
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String name;
}
