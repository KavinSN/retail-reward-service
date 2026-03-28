package com.retailrewards.controller;

import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.service.RewardService;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * Returns reward summary and transaction details for the requested customer and filter criteria.
     *
     * @param customerId customer identifier from the request path
     * @param months optional rolling month count
     * @param startDate optional range start date
     * @param endDate optional range end date
     * @return reward response for the requested customer
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardResponse> getRewards(
            @PathVariable @NotBlank String customerId,
            @RequestParam(required = false) @Min(1) Integer months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(rewardService.getCustomerRewards(customerId, months, startDate, endDate));
    }
}
