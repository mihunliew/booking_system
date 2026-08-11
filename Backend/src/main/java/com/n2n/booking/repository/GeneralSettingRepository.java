package com.n2n.booking.repository;

import com.n2n.booking.entity.GeneralSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralSettingRepository extends JpaRepository<GeneralSetting, Long> {
    List<GeneralSetting> findBySettingTypeAndActiveTrue(String settingType);
    List<GeneralSetting> findBySettingType(String settingType);
}
