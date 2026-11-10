package com.usercontent.domain.port.in;

import com.usercontent.domain.model.UserAggregate;

public interface SyncUserUseCase {

    UserAggregate sync(long userId);
}
