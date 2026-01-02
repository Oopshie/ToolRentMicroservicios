package com.toolrent.ms_tool.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Tool")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer replacementValue;

    @Column(nullable = false)
    private Boolean active = true;
}
