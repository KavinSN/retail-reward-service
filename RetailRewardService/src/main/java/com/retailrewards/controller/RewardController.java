package com.retailrewards.controller;

import com.retailrewards.dto.request.CustomerRewardRequest;
import com.retailrewards.dto.response.CustomerRewardResponse;
import com.retailrewards.service.RewardService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @PostMapping("/customers")
    public ResponseEntity<CustomerRewardResponse> getRewards(@Valid @RequestBody CustomerRewardRequest request) {
        return ResponseEntity.ok(rewardService.getCustomerRewards(request.getCustomerId(), request.getMonths(),
                request.getStartDate(), request.getEndDate()));
    }
}
