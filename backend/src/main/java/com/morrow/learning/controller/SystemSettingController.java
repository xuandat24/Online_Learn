package com.morrow.learning.controller;

import com.morrow.learning.domain.SystemSetting;
import com.morrow.learning.repository.SystemSettingRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/settings")
public class SystemSettingController {
    private final SystemSettingRepository settingRepository;

    public SystemSettingController(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping
    public List<SystemSetting> list() {
        return settingRepository.findAllByOrderBySettingGroupAscDisplayOrderAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SystemSetting create(@RequestBody SystemSetting setting) {
        return settingRepository.save(setting);
    }

    @PutMapping("/{id}")
    public SystemSetting update(@PathVariable Long id, @RequestBody SystemSetting updated) {
        var setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));
        setting.setSettingGroup(updated.getSettingGroup());
        setting.setName(updated.getName());
        setting.setValue(updated.getValue());
        setting.setDisplayOrder(updated.getDisplayOrder());
        setting.setActive(updated.isActive());
        return settingRepository.save(setting);
    }
}
