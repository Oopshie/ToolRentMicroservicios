package com.toolrent.ms_tool.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kardex {
    private Long id;

    // 1 = Préstamo
    // 2 = Devolución
    // 3 = Baja
    // 4 = Ingreso manual (opcional)
    private int movementType;

    // Fecha/hora en formato ISO (LocalDateTime.toString())
    private String movementDate;

    // Cantidad de herramientas involucradas
    private int quantity;

    // Herramienta asociada
    private Long toolId;

    // Usuario que generó el movimiento
    private String employeeName;

}