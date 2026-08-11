package com.n2n.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoleDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminPermissionDTO {
        @NotBlank
        private String moduleName;
        private boolean canCreate;
        private boolean canRead;
        private boolean canUpdate;
        private boolean canDelete;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminRoleRequest {
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private List<AdminPermissionDTO> permissions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminRoleResponse {
        private Long id;
        private String name;
        private String description;
        private List<AdminPermissionDTO> permissions;
    }
}
