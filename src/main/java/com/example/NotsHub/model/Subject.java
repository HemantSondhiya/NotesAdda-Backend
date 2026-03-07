package com.example.NotsHub.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name; // e.g. Data Structures

    @Column(unique = true)
    private String slug;

    @Column(length = 30)
    private String code; // e.g. CS301

    private Short credits;

    @Column(name = "syllabus_url")
    private String syllabusUrl;

    // Many Subjects → 1 Semester
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    @ToString.Exclude
    private Semester semester;

    // 1 Subject → Many Notes
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notes> notes = new ArrayList<>();
}
