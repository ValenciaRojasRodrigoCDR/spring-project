package com.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExcelData {

    private List<PlayerStats> players;
    private List<MatchInfo> matches;
}
