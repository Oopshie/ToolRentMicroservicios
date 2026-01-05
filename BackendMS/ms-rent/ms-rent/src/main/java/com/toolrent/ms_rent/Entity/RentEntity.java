package com.toolrent.ms_rent.Entity;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.toolrent.ms_rent.Model.Client;
import com.toolrent.ms_rent.Model.Tool;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "Rents")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (unique = true, nullable = false)
    private long id;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "tool_id")
    private Long toolId;

    private String employeeName;

    private String startDate;
    private String finishDate;
    private String returnDate;

    private int fineAmount;
    private int totalAmount;

    boolean active;

    boolean damaged;
    boolean irreparable;

}
