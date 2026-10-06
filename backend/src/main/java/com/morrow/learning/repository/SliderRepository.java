package com.morrow.learning.repository;

import com.morrow.learning.domain.Slider;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SliderRepository extends JpaRepository<Slider, Long> {
    List<Slider> findByStatusOrderByDisplayOrderAsc(String status);
    List<Slider> findAllByOrderByDisplayOrderAsc();
}
