package com.toolrent.ms_rate.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "Rate")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private int dailyRentalRate;
    private int dailyLateFeeRent;
}
