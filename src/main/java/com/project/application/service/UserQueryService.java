package com.project.application.service;

import com.project.application.port.in.GetUserQuery;
import com.project.application.port.out.UserRepository;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService implements GetUserQuery {

    private final UserRepository userRepository;

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
