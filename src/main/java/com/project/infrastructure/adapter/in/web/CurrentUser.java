package com.project.infrastructure.adapter.in.web;

import com.project.infrastructure.config.AuthDetails;
import org.springframework.security.core.Authentication;

final class CurrentUser {

    private CurrentUser() {}

    static Long userId(Authentication auth) {
        return auth != null && auth.getDetails() instanceof AuthDetails d ? d.userId() : null;
    }

    static Long jugadorId(Authentication auth) {
        return auth != null && auth.getDetails() instanceof AuthDetails d ? d.jugadorId() : null;
    }
}
