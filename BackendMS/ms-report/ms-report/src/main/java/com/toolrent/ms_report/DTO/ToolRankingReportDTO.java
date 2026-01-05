package com.toolrent.ms_report.DTO;

public class ToolRankingReportDTO {
    public String toolName;
    public String category;
    public Long rentalCount;

    public ToolRankingReportDTO( String toolName, String category,
                                Long rentalCount) {
        this.toolName = toolName;
        this.category = category;
        this.rentalCount = rentalCount;
    }


}
