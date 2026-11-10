package com.usercontent.domain.port.in;

import com.usercontent.domain.model.Post;

import java.util.List;

public interface GetTopPostsUseCase {

    List<Post> getTopPosts(int limit);
}
