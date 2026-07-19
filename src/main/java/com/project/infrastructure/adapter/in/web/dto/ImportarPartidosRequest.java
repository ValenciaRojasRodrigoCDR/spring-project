package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportarPartidosRequest(
        @NotEmpty List<@Valid CreatePartidoRequest> partidos
) {}
