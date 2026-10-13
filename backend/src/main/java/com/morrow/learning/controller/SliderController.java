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

import com.morrow.learning.service.MarketingService;

@RestController
public class SliderController {
    private final MarketingService marketingService;
    private final SliderRepository sliderRepository;

    public SliderController(MarketingService marketingService, SliderRepository sliderRepository) {
        this.marketingService = marketingService;
        this.sliderRepository = sliderRepository;
    }

    @GetMapping("/api/sliders")
    public List<Slider> getActiveSliders() {
        return sliderRepository.findByStatusOrderByDisplayOrderAsc("ACTIVE");
    }

    @GetMapping("/api/admin/sliders")
    public List<Slider> getAllAdminSliders() {
        return marketingService.getAllSliders();
    }

    @PostMapping("/api/admin/sliders")
    @ResponseStatus(HttpStatus.CREATED)
    public Slider createSlider(@RequestBody Slider slider) {
        return marketingService.createSlider(slider);
    }

    @PutMapping("/api/admin/sliders/{id}")
    public Slider updateSlider(@PathVariable Long id, @RequestBody Slider updated) {
        return marketingService.updateSlider(id, updated);
    }

    @DeleteMapping("/api/admin/sliders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlider(@PathVariable Long id) {
        marketingService.deleteSlider(id);
    }
}
