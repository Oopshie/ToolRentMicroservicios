package com.toolrent.ms_report.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Tool {
    private Long id;
    private String name;
    private String category;

}