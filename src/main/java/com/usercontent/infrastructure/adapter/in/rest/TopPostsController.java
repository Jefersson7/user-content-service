package com.usercontent.infrastructure.adapter.in.rest;

import com.usercontent.domain.port.in.GetTopPostsUseCase;
import com.usercontent.infrastructure.adapter.in.rest.dto.TopPostResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TopPostsController {

    private final GetTopPostsUseCase getTopPostsUseCase;

    public TopPostsController(GetTopPostsUseCase getTopPostsUseCase) {
        this.getTopPostsUseCase = getTopPostsUseCase;
    }

    @GetMapping("/posts/top")
    public List<TopPostResponse> getTopPosts(
            @RequestParam(defaultValue = "3") @Min(1) @Max(100) int limit) {
        return getTopPostsUseCase.getTopPosts(limit).stream()
                .map(post -> new TopPostResponse(post.id(), post.userId(), post.title(), post.body()))
                .toList();
    }
}
