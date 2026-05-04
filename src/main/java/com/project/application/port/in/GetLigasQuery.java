package com.project.application.port.in;

import com.project.domain.model.Liga;

import java.util.List;

public interface GetLigasQuery {
    List<Liga> getByUserId(Long userId);
    Liga getById(Long id);
}
