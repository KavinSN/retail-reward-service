package com.retailrewards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyRewardPoints {

    private final int year;
    private final String month;
    private final long rewardPoints;
}
