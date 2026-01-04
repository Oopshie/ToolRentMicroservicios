package com.toolrent.ms_kardex.Controller;

import com.toolrent.ms_kardex.Model.KardexDTO;
import com.toolrent.ms_kardex.Service.KardexService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin
public class KardexController {

    @Autowired
    private KardexService kardexService;

    @GetMapping("/tool/{toolId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<KardexDTO>> getByTool(@PathVariable Long toolId) {
        return ResponseEntity.ok(kardexService.getByTool(toolId));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<KardexDTO>> getByDateRange(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(kardexService.getByDateRange(from, to));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<KardexDTO>> getAll() {
        return ResponseEntity.ok(kardexService.getAllMovements());
    }

    @PostMapping("/internal")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<Void> register(@RequestBody KardexDTO dto) {
        kardexService.registerMovement(
                dto.getMovementType(),
                dto.getToolId(),
                dto.getEmployeeName()
        );
        return ResponseEntity.ok().build();
    }

}