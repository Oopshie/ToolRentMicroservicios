package com.toolrent.ms_rent.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Client {
    private Long id;

    private String rut;
    private String name;
    private String email;
    private String phoneNumber;
    private int status; // Active = 1, restricted = 0

}
