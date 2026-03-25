package com.retailrewards.dto.request;

import com.retailrewards.util.ApplicationConstants;
import java.time.LocalDate;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class CustomerRewardRequest {

    @NotBlank(message = ApplicationConstants.MESSAGE_CUSTOMER_ID_REQUIRED)
    private String customerId;

    @Min(value = 1, message = ApplicationConstants.MESSAGE_INVALID_MONTHS)
    private Integer months;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @AssertTrue(message = ApplicationConstants.MESSAGE_INVALID_DATE_RANGE)
    public boolean isDateRangeValid() {
        if (startDate == null && endDate == null) {
            return true;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }
}
