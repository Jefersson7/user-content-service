package com.usercontent.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record UserAggregate(User user, List<Post> posts, Map<Long, List<Comment>> commentsByPostId) {

    public UserAggregate {
        posts = List.copyOf(posts);
        commentsByPostId = Collections.unmodifiableMap(new LinkedHashMap<>(commentsByPostId));
    }

    public int totalComments() {
        return commentsByPostId.values().stream().mapToInt(List::size).sum();
    }
}
