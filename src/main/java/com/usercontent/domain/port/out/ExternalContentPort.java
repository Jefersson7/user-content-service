package com.usercontent.domain.port.out;

import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;

import java.util.List;

public interface ExternalContentPort {

    User fetchUser(long userId);

    List<Post> fetchPostsByUser(long userId);

    List<Comment> fetchCommentsByPost(long postId);
}
