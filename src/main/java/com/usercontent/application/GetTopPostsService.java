package com.usercontent.application;

import com.usercontent.domain.model.Post;
import com.usercontent.domain.port.in.GetTopPostsUseCase;
import com.usercontent.domain.port.out.PostQueryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTopPostsService implements GetTopPostsUseCase {

    private final PostQueryPort postQuery;

    public GetTopPostsService(PostQueryPort postQuery) {
        this.postQuery = postQuery;
    }

    @Override
    @Cacheable(cacheNames = "topPosts", key = "#limit")
    public List<Post> getTopPosts(int limit) {
        return postQuery.findTop(limit);
    }
}
