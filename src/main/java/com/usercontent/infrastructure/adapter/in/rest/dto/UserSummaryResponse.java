package com.usercontent.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record UserSummaryResponse(
        long userId,
        String userName,
        int totalPost,
        int totalComments,
        List<PostSummaryResponse> posts) {
}
