package com.earn.earnmoney.Service;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CounterActionResponse {

    private String action; // "started", "claimed", or "restarted"
    private Integer pointsEarned;
    private Integer totalPoints;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime nextAvailableAt;

    public static CounterActionResponse started(LocalDateTime nextAvailableAt) {
        return new CounterActionResponse("started", null, null, nextAvailableAt);
    }

    public static CounterActionResponse claimed(int pointsEarned, int totalPoints) {
        return new CounterActionResponse("claimed", pointsEarned, totalPoints, null);
    }

    public static CounterActionResponse restarted(int pointsEarned, int totalPoints, LocalDateTime nextAvailableAt) {
        return new CounterActionResponse("restarted", pointsEarned, totalPoints, nextAvailableAt);
    }
}
