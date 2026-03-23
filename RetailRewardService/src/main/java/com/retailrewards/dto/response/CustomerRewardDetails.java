package com.retailrewards.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerRewardDetails {

    private final String customerId;
    private final String customerName;
    private final Map<String, Long> monthlyPoints;
    private final long totalPoints;
}
