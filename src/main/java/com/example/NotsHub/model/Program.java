package com.example.NotsHub.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name; // BTech, MCA, MBA, BCA

    @Column(nullable = false, length = 20)
    private String type; // UG | PG | DIPLOMA

    private Short duration; // years

    // Many Programs → 1 College
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    @ToString.Exclude
    private College college;

    // 1 Program → Many Branches
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();
}

