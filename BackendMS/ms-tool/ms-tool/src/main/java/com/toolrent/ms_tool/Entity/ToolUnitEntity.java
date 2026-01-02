package com.toolrent.ms_tool.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Tool_Unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolUnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tool_id", nullable=false)
    private ToolEntity tool;

    @Column(nullable = false)
    private int status; // Available = 1, Lent = 2, Under repair = 3, Decommissioned = 4

}
