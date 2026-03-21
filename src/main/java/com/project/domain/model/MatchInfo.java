package com.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MatchInfo {

    private String jornada;
    private String rival;
    private LocalDate date;
    private String result;          
    private Integer teamTotalGoals;
}
