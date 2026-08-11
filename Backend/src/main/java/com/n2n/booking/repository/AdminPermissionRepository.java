package com.n2n.booking.repository;

import com.n2n.booking.entity.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {
    List<AdminPermission> findByAdminRoleId(Long adminRoleId);
    Optional<AdminPermission> findByAdminRoleIdAndModuleName(Long adminRoleId, String moduleName);
    void deleteByAdminRoleId(Long adminRoleId);
}
