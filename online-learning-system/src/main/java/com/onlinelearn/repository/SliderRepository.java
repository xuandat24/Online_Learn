package com.onlinelearn.repository;

import com.onlinelearn.entity.Slider;
import com.onlinelearn.entity.enums.SliderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SliderRepository extends JpaRepository<Slider, Long> {
    List<Slider> findByStatus(SliderStatus status);
}
