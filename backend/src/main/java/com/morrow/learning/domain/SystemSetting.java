package com.morrow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_settings")
public class SystemSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String settingGroup;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "setting_value", nullable = false, length = 100)
    private String value;

    @Column(nullable = false)
    private Integer displayOrder = 1;

    @Column(nullable = false)
    private boolean active = true;

    protected SystemSetting() {}

    public SystemSetting(String settingGroup, String name, String value, Integer displayOrder, boolean active) {
        this.settingGroup = settingGroup;
        this.name = name;
        this.value = value;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getSettingGroup() { return settingGroup; }
    public void setSettingGroup(String settingGroup) { this.settingGroup = settingGroup; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
