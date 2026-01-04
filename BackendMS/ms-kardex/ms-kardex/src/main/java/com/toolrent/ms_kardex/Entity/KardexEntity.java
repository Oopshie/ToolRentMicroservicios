package com.toolrent.ms_kardex.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    // 1 = Préstamo
    // 2 = Devolución
    // 3 = Baja
    // 4 = Ingreso manual (opcional)
    private int movementType;

    // Fecha/hora en formato ISO (LocalDateTime.toString())
    private String movementDate;

    // Cantidad de herramientas involucradas (en tu caso siempre 1)
    private int quantity;

    // Herramienta asociada
    private Long toolId;

    // Usuario que generó el movimiento (empleado/admin)
    private String employeeName;

}
