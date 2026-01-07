package com.toolrent.ms_report.Controller;

import com.toolrent.ms_report.DTO.ActiveRentReportDTO;
import com.toolrent.ms_report.DTO.LateClientReportDTO;
import com.toolrent.ms_report.DTO.ToolRankingReportDTO;
import com.toolrent.ms_report.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    // RF6.1 Listar préstamos activos
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/active-rents")
    public ResponseEntity<List<ActiveRentReportDTO>> getActiveRents(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getActiveRents(from, to));
    }

    // RF6.2 Listar clientes con atrasos
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/late-clients")
    public ResponseEntity<List<LateClientReportDTO>> getLateClients(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getLateClients(from, to));
    }

    // RF6.3 Ranking de herramientas
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/ranking")
    public ResponseEntity<List<ToolRankingReportDTO>> getToolRanking(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getToolRanking(from, to));
    }
}