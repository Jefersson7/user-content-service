package com.usercontent.domain.port.in;

import com.usercontent.domain.model.UserAggregate;

public interface GetUserSummaryUseCase {

    UserAggregate getSummary(long userId);
}
