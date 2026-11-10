package com.usercontent.application;

import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserSummaryServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private GetUserSummaryService service;

    @Test
    void returnsStoredAggregate() {
        User user = new User(1L, "Leanne Graham", "Bret", "Sincere@april.biz");
        Post post = new Post(1L, 1L, "title", "body");
        Comment comment = new Comment(1L, 1L, "name", "email", "comment");
        UserAggregate aggregate = new UserAggregate(user, List.of(post), Map.of(1L, List.of(comment)));

        when(userRepository.findAggregate(1L)).thenReturn(Optional.of(aggregate));

        assertThat(service.getSummary(1L)).isSameAs(aggregate);
        verify(userRepository).findAggregate(1L);
    }

    @Test
    void failsWhenUserIsNotStored() {
        when(userRepository.findAggregate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSummary(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User 99 not found");
    }
}
