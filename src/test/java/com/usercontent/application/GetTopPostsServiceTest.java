package com.usercontent.application;

import com.usercontent.domain.model.Post;
import com.usercontent.domain.port.out.PostQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopPostsServiceTest {

    @Mock
    private PostQueryPort postQuery;

    @InjectMocks
    private GetTopPostsService service;

    @Test
    void returnsTopPosts() {
        Post post = new Post(100L, 10L, "title", "body");

        when(postQuery.findTop(3)).thenReturn(List.of(post));

        assertThat(service.getTopPosts(3)).containsExactly(post);
        verify(postQuery).findTop(3);
    }

    @Test
    void returnsEmptyWhenThereAreNoPosts() {
        when(postQuery.findTop(5)).thenReturn(List.of());

        assertThat(service.getTopPosts(5)).isEmpty();
    }
}
