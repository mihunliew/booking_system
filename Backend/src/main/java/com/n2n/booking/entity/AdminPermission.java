package com.n2n.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"admin_role_id", "module_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_role_id", nullable = false)
    private AdminRole adminRole;

    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName; // e.g. "USERS", "PRODUCTS", "BOOKINGS", "ROLES", "PROMOCODES"

    @Column(name = "can_create", nullable = false)
    private boolean canCreate;

    @Column(name = "can_read", nullable = false)
    private boolean canRead;

    @Column(name = "can_update", nullable = false)
    private boolean canUpdate;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete;
}
