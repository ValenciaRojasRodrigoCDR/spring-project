package com.project.application.port.in;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements) {
}
