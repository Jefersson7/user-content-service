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

    static PostEntity toEntity(Post post, UserEntity user) {
        return new PostEntity(post.id(), user, post.title(), post.body());
    }

    static CommentEntity toEntity(Comment comment, PostEntity post) {
        return new CommentEntity(comment.id(), post, comment.name(), comment.email(), comment.body());
    }
}
