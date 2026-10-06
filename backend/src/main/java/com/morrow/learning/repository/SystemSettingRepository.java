package com.morrow.learning.repository;

import com.morrow.learning.domain.SystemSetting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    List<SystemSetting> findAllByOrderBySettingGroupAscDisplayOrderAsc();
    List<SystemSetting> findBySettingGroupAndActiveTrueOrderByDisplayOrderAsc(String settingGroup);
}
