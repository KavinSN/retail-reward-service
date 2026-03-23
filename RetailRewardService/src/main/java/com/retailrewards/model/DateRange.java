package com.retailrewards.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;
}
