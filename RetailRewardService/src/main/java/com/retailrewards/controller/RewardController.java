package com.retailrewards.controller;

import com.retailrewards.dto.request.CustomerTransactionRequest;
import com.retailrewards.dto.request.RewardRequest;
import com.retailrewards.dto.response.CustomerRewardDetails;
import com.retailrewards.dto.response.CustomerTransactionResponse;
import com.retailrewards.service.RewardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public List<CustomerRewardDetails> getRewards(@RequestBody(required = false) RewardRequest request) {
        RewardRequest rewardRequest = request == null ? new RewardRequest() : request;
        return rewardService.calculateRewards(rewardRequest.getCustomerId(), rewardRequest.getMonths(),
                rewardRequest.getStartDate(), rewardRequest.getEndDate());
    }

    @PostMapping("/customers/transactions")
    public List<CustomerTransactionResponse> getCustomerTransactions(
            @RequestBody(required = false) CustomerTransactionRequest request) {
        CustomerTransactionRequest transactionRequest = request == null ? new CustomerTransactionRequest() : request;
        return rewardService.getCustomerTransactions(transactionRequest.getCustomerId(), transactionRequest.getMonths(),
                transactionRequest.getStartDate(), transactionRequest.getEndDate());
    }
}
