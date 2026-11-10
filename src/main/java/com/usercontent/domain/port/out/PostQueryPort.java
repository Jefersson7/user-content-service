package com.usercontent.domain.port.out;

import com.usercontent.domain.model.Post;

import java.util.List;

public interface PostQueryPort {

    List<Post> findTop(int limit);
}
