package com.project.application.port.in;

import com.project.domain.model.User;

public interface GetUserQuery {
    User getByUsername(String username);
}
