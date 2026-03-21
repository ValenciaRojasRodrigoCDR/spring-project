package com.project.application.port.in;

public interface LoginUseCase {

    record LoginCommand(String username, String password) {}

    String login(LoginCommand command);
}
