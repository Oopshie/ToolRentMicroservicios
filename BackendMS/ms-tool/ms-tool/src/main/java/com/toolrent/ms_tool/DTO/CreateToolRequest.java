package com.toolrent.ms_tool.DTO;

import lombok.Data;

@Data
public class CreateToolRequest {

    private String name;
    private String category;
    private Integer replacementValue;
    private Integer quantity;
}
