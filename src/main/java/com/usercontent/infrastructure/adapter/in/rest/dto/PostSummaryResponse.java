package com.usercontent.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record PostSummaryResponse(long id, String title, List<CommentSummaryResponse> comment) {
}
