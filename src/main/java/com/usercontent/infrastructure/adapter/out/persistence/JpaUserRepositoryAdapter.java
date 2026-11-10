package com.usercontent.infrastructure.adapter.out.persistence;

import com.usercontent.domain.model.User;
import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.out.UserRepositoryPort;
import com.usercontent.infrastructure.adapter.out.persistence.entity.CommentEntity;
import com.usercontent.infrastructure.adapter.out.persistence.entity.PostEntity;
import com.usercontent.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.usercontent.infrastructure.adapter.out.persistence.repository.CommentJpaRepository;
import com.usercontent.infrastructure.adapter.out.persistence.repository.PostJpaRepository;
import com.usercontent.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository users;
    private final PostJpaRepository posts;
    private final CommentJpaRepository comments;
    private final EntityManager entityManager;

    public JpaUserRepositoryAdapter(UserJpaRepository users,
                                    PostJpaRepository posts,
                                    CommentJpaRepository comments,
                                    EntityManager entityManager) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void replaceAggregate(UserAggregate aggregate) {
        User user = aggregate.user();
        users.saveAndFlush(PersistenceMapper.toEntity(user));

        comments.deleteByUserId(user.id());
        posts.deleteByUserId(user.id());

        UserEntity userReference = users.getReferenceById(user.id());
        List<PostEntity> postEntities = aggregate.posts().stream()
                .map(post -> PersistenceMapper.toEntity(post, userReference))
                .toList();
        postEntities.forEach(entityManager::persist);
        entityManager.flush();

        List<CommentEntity> commentEntities = aggregate.commentsByPostId().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(comment -> PersistenceMapper.toEntity(
                                comment, posts.getReferenceById(entry.getKey()))))
                .toList();
        commentEntities.forEach(entityManager::persist);
    }
}
