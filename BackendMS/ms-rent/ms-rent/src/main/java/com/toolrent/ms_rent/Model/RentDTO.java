package com.toolrent.ms_rent.Model;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentDTO {

    private Long id;

    private String clientName;
    private String toolName;

    private Long clientId;
    private Long toolId;

    private String startDate;
    private String finishDate;
    private String returnDate;

    private boolean active;

    private boolean damaged;
    private boolean irreparable;

    private int fineAmount;
    private int totalAmount;

    private String employeeName;




}
