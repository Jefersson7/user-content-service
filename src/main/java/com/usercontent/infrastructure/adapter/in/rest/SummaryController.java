package com.usercontent.infrastructure.adapter.in.rest;

import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.in.GetUserSummaryUseCase;
import com.usercontent.infrastructure.adapter.in.rest.dto.CommentSummaryResponse;
import com.usercontent.infrastructure.adapter.in.rest.dto.PostSummaryResponse;
import com.usercontent.infrastructure.adapter.in.rest.dto.UserSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SummaryController {

    private final GetUserSummaryUseCase getUserSummaryUseCase;

    public SummaryController(GetUserSummaryUseCase getUserSummaryUseCase) {
        this.getUserSummaryUseCase = getUserSummaryUseCase;
    }

    @GetMapping("/users/{id}/summary")
    public UserSummaryResponse getSummary(@PathVariable long id) {
        UserAggregate aggregate = getUserSummaryUseCase.getSummary(id);

        List<PostSummaryResponse> posts = aggregate.posts().stream()
                .map(post -> new PostSummaryResponse(
                        post.id(),
                        post.title(),
                        commentsFor(aggregate, post.id())))
                .toList();

        return new UserSummaryResponse(
                aggregate.user().id(),
                aggregate.user().username(),
                aggregate.posts().size(),
                aggregate.totalComments(),
                posts);
    }

    private static List<CommentSummaryResponse> commentsFor(UserAggregate aggregate, long postId) {
        return aggregate.commentsByPostId().getOrDefault(postId, List.of()).stream()
                .map(comment -> new CommentSummaryResponse(comment.id(), comment.body()))
                .toList();
    }
}
