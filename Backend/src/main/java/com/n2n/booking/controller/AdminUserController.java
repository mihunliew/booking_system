package com.n2n.booking.controller;

import com.n2n.booking.dto.AuthDTOs;
import com.n2n.booking.enums.Role;
import com.n2n.booking.service.AdminService;
import com.n2n.booking.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'USERS_READ')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<AuthDTOs.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'USERS_CREATE')")
    public ResponseEntity<AuthDTOs.UserResponse> createUser(
            @RequestBody @jakarta.validation.Valid AuthDTOs.AdminCreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(adminService.createUser(request, currentUser.getId()));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'USERS_UPDATE')")
    public ResponseEntity<AuthDTOs.UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @RequestParam(required = false) Long adminRoleId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role, adminRoleId, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'USERS_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        adminService.deleteUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
