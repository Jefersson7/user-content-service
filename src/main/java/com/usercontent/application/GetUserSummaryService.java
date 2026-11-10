package com.usercontent.application;

import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.UserAggregate;
import com.usercontent.domain.port.in.GetUserSummaryUseCase;
import com.usercontent.domain.port.out.UserRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GetUserSummaryService implements GetUserSummaryUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserSummaryService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(cacheNames = "userSummary", key = "#userId")
    public UserAggregate getSummary(long userId) {
        return userRepository.findAggregate(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
