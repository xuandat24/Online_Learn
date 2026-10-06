package com.morrow.learning.controller;

import com.morrow.learning.domain.Slider;
import com.morrow.learning.repository.SliderRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SliderController {
    private final SliderRepository sliderRepository;

    public SliderController(SliderRepository sliderRepository) {
        this.sliderRepository = sliderRepository;
    }

    @GetMapping("/api/sliders")
    public List<Slider> getActiveSliders() {
        return sliderRepository.findByStatusOrderByDisplayOrderAsc("ACTIVE");
    }

    @GetMapping("/api/admin/sliders")
    public List<Slider> getAllAdminSliders() {
        return sliderRepository.findAllByOrderByDisplayOrderAsc();
    }

    @PostMapping("/api/admin/sliders")
    @ResponseStatus(HttpStatus.CREATED)
    public Slider createSlider(@RequestBody Slider slider) {
        return sliderRepository.save(slider);
    }

    @PutMapping("/api/admin/sliders/{id}")
    public Slider updateSlider(@PathVariable Long id, @RequestBody Slider updated) {
        var slider = sliderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slider not found"));
        slider.setTitle(updated.getTitle());
        slider.setTag(updated.getTag());
        slider.setImage(updated.getImage());
        slider.setBacklink(updated.getBacklink());
        slider.setStatus(updated.getStatus());
        slider.setDisplayOrder(updated.getDisplayOrder());
        return sliderRepository.save(slider);
    }

    @DeleteMapping("/api/admin/sliders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlider(@PathVariable Long id) {
        sliderRepository.deleteById(id);
    }
}
