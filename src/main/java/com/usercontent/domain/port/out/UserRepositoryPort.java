package com.usercontent.domain.port.out;

import com.usercontent.domain.model.UserAggregate;

import java.util.Optional;

public interface UserRepositoryPort {

    void replaceAggregate(UserAggregate aggregate);

    Optional<UserAggregate> findAggregate(long userId);
}
