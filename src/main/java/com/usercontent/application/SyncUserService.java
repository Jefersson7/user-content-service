package com.usercontent.application;

import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.in.SyncUserUseCase;
import com.usercontent.domain.port.out.ExternalContentPort;
import com.usercontent.domain.port.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SyncUserService implements SyncUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncUserService.class);

    private final ExternalContentPort externalContent;
    private final UserRepositoryPort userRepository;

    public SyncUserService(ExternalContentPort externalContent, UserRepositoryPort userRepository) {
        this.externalContent = externalContent;
        this.userRepository = userRepository;
    }

    @Override
    public UserAggregate sync(long userId) {
        long start = System.nanoTime();
        log.info("Sync started userId={}", userId);
        try {
            User user = externalContent.fetchUser(userId);
            List<Post> posts = externalContent.fetchPostsByUser(userId);

            Map<Long, List<Comment>> commentsByPostId = new LinkedHashMap<>();
            for (Post post : posts) {
                commentsByPostId.put(post.id(), externalContent.fetchCommentsByPost(post.id()));
            }

            UserAggregate aggregate = new UserAggregate(user, posts, commentsByPostId);
            userRepository.replaceAggregate(aggregate);

            log.info("Sync completed userId={} posts={} comments={} durationMs={}",
                    userId, posts.size(), aggregate.totalComments(), elapsedMillis(start));
            return aggregate;
        } catch (UserNotFoundException exception) {
            log.info("Sync failed userId={} durationMs={} reason={}",
                    userId, elapsedMillis(start), exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Sync failed userId={} durationMs={} reason={}",
                    userId, elapsedMillis(start), exception.getMessage());
            throw exception;
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
