package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceStatsResponse {
    private long totalSessions;
    private long presentCount;
    private long absentCount;
    private double attendanceRate;
}
