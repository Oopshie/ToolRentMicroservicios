package com.toolrent.ms_report.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Rent {
    private Long id;
    private String clientName;
    private String toolName;
    private Long clientId;
    private Long toolId;
    private String startDate;
    private String finishDate; // Fecha pactada
    private String returnDate; // Fecha real (puede ser null)
    private boolean active;
    private int totalAmount;

}