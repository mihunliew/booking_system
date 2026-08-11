package com.n2n.booking.dto;

import lombok.Builder;
import lombok.Data;

public class SettingDTOs {

    @Data
    public static class SettingRequest {
        private String settingType;
        private String name;
        private String providerKey;
        private String icon;
        private String description;
        private boolean active;
    }

    @Data
    @Builder
    public static class SettingResponse {
        private Long id;
        private String settingType;
        private String name;
        private String providerKey;
        private String icon;
        private String description;
        private boolean active;
    }
}
