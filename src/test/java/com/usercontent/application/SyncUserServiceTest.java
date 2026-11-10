package com.usercontent.application;

import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.out.ExternalContentPort;
import com.usercontent.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncUserServiceTest {

    @Mock
    private ExternalContentPort externalContent;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private SyncUserService service;

    @Test
    void syncsUserWithPostsAndComments() {
        User user = new User(1L, "Leanne Graham", "Bret", "Sincere@april.biz");
        Post firstPost = new Post(1L, 1L, "first title", "first body");
        Post secondPost = new Post(2L, 1L, "second title", "second body");
        List<Comment> firstPostComments = List.of(
                new Comment(1L, 1L, "name", "email", "first comment"),
                new Comment(2L, 1L, "name", "email", "second comment"));

        when(externalContent.fetchUser(1L)).thenReturn(user);
        when(externalContent.fetchPostsByUser(1L)).thenReturn(List.of(firstPost, secondPost));
        when(externalContent.fetchCommentsByPost(1L)).thenReturn(firstPostComments);
        when(externalContent.fetchCommentsByPost(2L)).thenReturn(List.of());

        UserAggregate aggregate = service.sync(1L);

        assertThat(aggregate.user()).isEqualTo(user);
        assertThat(aggregate.posts()).containsExactly(firstPost, secondPost);
        assertThat(aggregate.commentsByPostId()).containsEntry(1L, firstPostComments);
        assertThat(aggregate.totalComments()).isEqualTo(2);
        verify(userRepository).replaceAggregate(aggregate);
    }

    @Test
    void failsWhenUserDoesNotExist() {
        when(externalContent.fetchUser(99L)).thenThrow(new UserNotFoundException(99L));

        assertThatThrownBy(() -> service.sync(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User 99 not found");

        verifyNoInteractions(userRepository);
        verify(externalContent, never()).fetchPostsByUser(anyLong());
    }
}
