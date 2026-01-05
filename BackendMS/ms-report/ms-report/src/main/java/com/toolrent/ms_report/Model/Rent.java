package com.toolrent.ms_report.Model;

import lombok.Data;

@Data
public class Rent {
    private Long id;
    private Long clientId;
    private Long toolId;
    private String startDate;
    private String finishDate; // Fecha pactada
    private String returnDate; // Fecha real (puede ser null)
    private boolean active;
    private int totalAmount;
}