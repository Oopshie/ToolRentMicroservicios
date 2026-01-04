package com.toolrent.ms_kardex.Model;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Tool {

    private Long id;

    private String name;
    private String category;
    private Integer replacementValue;
    private int status; // Available = 1, Lent = 2, Under repair = 3, Decommissioned = 4

    private Integer stock;
}
