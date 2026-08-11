package com.n2n.booking.service;

import com.n2n.booking.dto.RoleDTOs;
import com.n2n.booking.entity.AdminPermission;
import com.n2n.booking.entity.AdminRole;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.AdminRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    public List<RoleDTOs.AdminRoleResponse> getAllRoles() {
        return adminRoleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RoleDTOs.AdminRoleResponse getRoleById(Long id) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return mapToDTO(role);
    }

    @Transactional
    public RoleDTOs.AdminRoleResponse createRole(RoleDTOs.AdminRoleRequest request) {
        if (adminRoleRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Role name already exists");
        }

        AdminRole role = AdminRole.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        List<AdminPermission> permissions = request.getPermissions().stream().map(dto -> AdminPermission.builder()
                .adminRole(role)
                .moduleName(dto.getModuleName())
                .canCreate(dto.isCanCreate())
                .canRead(dto.isCanRead())
                .canUpdate(dto.isCanUpdate())
                .canDelete(dto.isCanDelete())
                .build()
        ).collect(Collectors.toList());

        role.setPermissions(permissions);

        AdminRole savedRole = adminRoleRepository.save(role);
        return mapToDTO(savedRole);
    }

    @Transactional
    public RoleDTOs.AdminRoleResponse updateRole(Long id, RoleDTOs.AdminRoleRequest request) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!role.getName().equals(request.getName()) && adminRoleRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Role name already exists");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Clear existing and add new to simplify update (orphanRemoval will handle deletes)
        role.getPermissions().clear();

        List<AdminPermission> permissions = request.getPermissions().stream().map(dto -> AdminPermission.builder()
                .adminRole(role)
                .moduleName(dto.getModuleName())
                .canCreate(dto.isCanCreate())
                .canRead(dto.isCanRead())
                .canUpdate(dto.isCanUpdate())
                .canDelete(dto.isCanDelete())
                .build()
        ).collect(Collectors.toList());

        role.getPermissions().addAll(permissions);

        AdminRole savedRole = adminRoleRepository.save(role);
        return mapToDTO(savedRole);
    }

    @Transactional
    public void deleteRole(Long id) {
        AdminRole role = adminRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        // Optionally check if users are still assigned to this role before deleting
        // This can be handled by foreign key constraint catching as well
        adminRoleRepository.delete(role);
    }

    private RoleDTOs.AdminRoleResponse mapToDTO(AdminRole role) {
        return RoleDTOs.AdminRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream().map(p -> RoleDTOs.AdminPermissionDTO.builder()
                        .moduleName(p.getModuleName())
                        .canCreate(p.isCanCreate())
                        .canRead(p.isCanRead())
                        .canUpdate(p.isCanUpdate())
                        .canDelete(p.isCanDelete())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
