package com.n2n.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @OneToMany(mappedBy = "adminRole", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdminPermission> permissions = new ArrayList<>();
}
