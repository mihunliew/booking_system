package com.n2n.booking.controller;

import com.n2n.booking.dto.RoleDTOs;
import com.n2n.booking.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ROLES_READ')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public ResponseEntity<List<RoleDTOs.AdminRoleResponse>> getAllRoles() {
        return ResponseEntity.ok(adminRoleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTOs.AdminRoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(adminRoleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ROLES_CREATE')")
    public ResponseEntity<RoleDTOs.AdminRoleResponse> createRole(@Valid @RequestBody RoleDTOs.AdminRoleRequest request) {
        return ResponseEntity.ok(adminRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ROLES_UPDATE')")
    public ResponseEntity<RoleDTOs.AdminRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTOs.AdminRoleRequest request) {
        return ResponseEntity.ok(adminRoleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ROLES_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        adminRoleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
