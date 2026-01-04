package com.toolrent.ms_rent.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Rate {

    private Long id;

    private int dailyRentalRate;
    private int dailyLateFeeRent;
}
