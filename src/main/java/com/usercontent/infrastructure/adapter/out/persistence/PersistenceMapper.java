package com.usercontent.infrastructure.adapter.out.persistence;

import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import com.usercontent.infrastructure.adapter.out.persistence.entity.CommentEntity;
import com.usercontent.infrastructure.adapter.out.persistence.entity.PostEntity;
import com.usercontent.infrastructure.adapter.out.persistence.entity.UserEntity;

final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.name(), user.username(), user.email());
    }

    static User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getUsername(), entity.getEmail());
    }

    static PostEntity toEntity(Post post, UserEntity user) {
        return new PostEntity(post.id(), user, post.title(), post.body());
    }

    static Post toDomain(PostEntity entity, Long userId) {
        return new Post(entity.getId(), userId, entity.getTitle(), entity.getBody());
    }

    static CommentEntity toEntity(Comment comment, PostEntity post) {
        return new CommentEntity(comment.id(), post, comment.name(), comment.email(), comment.body());
    }

    static Comment toDomain(CommentEntity entity) {
        return new Comment(entity.getId(), entity.getPost().getId(), entity.getName(), entity.getEmail(), entity.getBody());
    }
}
