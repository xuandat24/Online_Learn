package com.morrow.learning.service;

import com.morrow.learning.domain.Post;
import com.morrow.learning.domain.Slider;
import com.morrow.learning.repository.PostRepository;
import com.morrow.learning.repository.SliderRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MarketingService {
    private final PostRepository postRepository;
    private final SliderRepository sliderRepository;

    public MarketingService(PostRepository postRepository, SliderRepository sliderRepository) {
        this.postRepository = postRepository;
        this.sliderRepository = sliderRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postRepository.count());
        stats.put("totalSliders", sliderRepository.count());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, Post updated) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        post.setTitle(updated.getTitle());
        post.setCategory(updated.getCategory());
        post.setAuthor(updated.getAuthor());
        post.setStatus(updated.getStatus());
        post.setSummary(updated.getSummary());
        post.setContent(updated.getContent());
        post.setImage(updated.getImage());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public void publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        post.setStatus("PUBLISHED");
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<Slider> getAllSliders() {
        return sliderRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Slider createSlider(Slider slider) {
        return sliderRepository.save(slider);
    }

    @Transactional
    public Slider updateSlider(Long id, Slider updated) {
        Slider slider = sliderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slider not found"));
        slider.setTitle(updated.getTitle());
        slider.setTag(updated.getTag());
        slider.setImage(updated.getImage());
        slider.setBacklink(updated.getBacklink());
        slider.setStatus(updated.getStatus());
        slider.setDisplayOrder(updated.getDisplayOrder());
        return sliderRepository.save(slider);
    }

    @Transactional
    public void deleteSlider(Long id) {
        sliderRepository.deleteById(id);
    }
}
