package com.usercontent.infrastructure.adapter.out.persistence;

import com.usercontent.domain.model.Post;
import com.usercontent.domain.port.out.PostQueryPort;
import com.usercontent.infrastructure.adapter.out.persistence.repository.PostJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaPostQueryAdapter implements PostQueryPort {

    private final PostJpaRepository posts;

    public JpaPostQueryAdapter(PostJpaRepository posts) {
        this.posts = posts;
    }

    @Override
    public List<Post> findTop(int limit) {
        return posts.findAllByOrderByIdDesc(PageRequest.of(0, limit)).stream()
                .map(post -> PersistenceMapper.toDomain(post, post.getUser().getId()))
                .toList();
    }
}
