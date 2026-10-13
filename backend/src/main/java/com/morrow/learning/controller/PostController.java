package com.morrow.learning.controller;

import com.morrow.learning.domain.Post;
import com.morrow.learning.repository.PostRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.morrow.learning.service.MarketingService;

@RestController
public class PostController {
    private final MarketingService marketingService;
    private final PostRepository postRepository;

    public PostController(MarketingService marketingService, PostRepository postRepository) {
        this.marketingService = marketingService;
        this.postRepository = postRepository;
    }

    @GetMapping("/api/posts")
    public List<Post> getPublicPosts(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank() && !"Tất cả".equalsIgnoreCase(category)) {
            return postRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, "PUBLISHED");
        }
        return postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED");
    }

    @GetMapping("/api/posts/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @GetMapping("/api/admin/posts")
    public List<Post> getAllAdminPosts() {
        return marketingService.getAllPosts();
    }

    @PostMapping("/api/admin/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@RequestBody Post post) {
        return marketingService.createPost(post);
    }

    @PutMapping("/api/admin/posts/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post updated) {
        return marketingService.updatePost(id, updated);
    }

    @DeleteMapping("/api/admin/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        marketingService.deletePost(id);
    }
}
