package com.usercontent.infrastructure.adapter.out.persistence;

import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAggregate> findAggregate(long userId) {
        return users.findById(userId).map(userEntity -> {
            List<PostEntity> postEntities = posts.findByUser_IdOrderByIdAsc(userId);
            List<Post> postList = postEntities.stream()
                    .map(entity -> PersistenceMapper.toDomain(entity, userId))
                    .toList();

            Map<Long, List<Comment>> commentsByPostId = new LinkedHashMap<>();
            postEntities.forEach(post -> commentsByPostId.put(post.getId(), new ArrayList<>()));

            if (!postEntities.isEmpty()) {
                List<Long> postIds = postEntities.stream().map(PostEntity::getId).toList();
                comments.findByPost_IdInOrderByIdAsc(postIds).stream()
                        .map(PersistenceMapper::toDomain)
                        .forEach(comment -> commentsByPostId.get(comment.postId()).add(comment));
            }

            return new UserAggregate(PersistenceMapper.toDomain(userEntity), postList, commentsByPostId);
        });
    }
}
