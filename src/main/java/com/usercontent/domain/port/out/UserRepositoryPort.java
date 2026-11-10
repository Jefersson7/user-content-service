package com.usercontent.domain.port.out;

import com.usercontent.domain.model.UserAggregate;

public interface UserRepositoryPort {

    void replaceAggregate(UserAggregate aggregate);
}
