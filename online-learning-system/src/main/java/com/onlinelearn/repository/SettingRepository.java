package com.onlinelearn.repository;

import com.onlinelearn.entity.Setting;
import com.onlinelearn.entity.enums.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    List<Setting> findByType(SettingType type);
    Optional<Setting> findByTypeAndCode(SettingType type, String code);
    boolean existsByTypeAndCode(SettingType type, String code);
}
