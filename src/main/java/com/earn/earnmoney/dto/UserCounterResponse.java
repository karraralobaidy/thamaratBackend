package com.earn.earnmoney.dto;

import com.earn.earnmoney.model.UserCounter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserCounterResponse {

    private Long id; // UserCounter ID
    private Long counterId;
    private String counterName;
    private int level;
    private int pointsPerClick;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lastClickedAt;
    private long cooldownHours;

    public static UserCounterResponse from(UserCounter userCounter) {
        return new UserCounterResponse(
                userCounter.getId(),
                userCounter.getCounter().getId(),
                userCounter.getCounter().getName(),
                userCounter.getLevel(),
                userCounter.getCurrentPackage() != null
                        ? userCounter.getCurrentPackage().getPointsPerClick()
                        : 0,
                userCounter.getLastClickedAt(),
                userCounter.getCounter().getCooldownHours());
    }
}
