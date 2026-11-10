package com.usercontent.infrastructure.adapter.in.rest;

import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.in.SyncUserUseCase;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final SyncUserUseCase syncUserUseCase;

    public SyncController(SyncUserUseCase syncUserUseCase) {
        this.syncUserUseCase = syncUserUseCase;
    }

    @PostMapping("/sync/{userId}")
    public SyncResponse sync(@PathVariable long userId) {
        UserAggregate aggregate = syncUserUseCase.sync(userId);
        return new SyncResponse(userId, aggregate.posts().size(), aggregate.totalComments());
    }

    public record SyncResponse(long userId, int posts, int comments) {
    }
}
