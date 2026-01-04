package com.toolrent.ms_kardex.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KardexDTO {

    private Long id;
    private String movementDate;
    private int movementType;
    private int quantity;

    private Long toolId;
    private String toolName;
    private String toolCategory;

    private String employeeName; // empleado
}
