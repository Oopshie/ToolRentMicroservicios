package com.toolrent.ms_client.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Client")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String rut;
    private String name;
    private String email;
    private String phoneNumber;
    private int status; // Active = 1, restricted = 0

}