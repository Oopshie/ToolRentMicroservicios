package com.toolrent.ms_rate.Controller;


import com.toolrent.ms_rate.Entity.RateEntity;
import com.toolrent.ms_rate.Service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rate")
@CrossOrigin
public class RateController {

    @Autowired
    private RateService rateService;

    // Obtener tarifa vigente
    @GetMapping("/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateEntity> getLatestRate() {
        RateEntity rate = rateService.getLatestRate();
        return ResponseEntity.ok(rate);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateEntity> createRate(@RequestBody Map<String, Integer> body) {

        int rental = body.get("dailyRentalRate");
        int late = body.get("dailyLateFeeRent");

        RateEntity created = rateService.createRate(rental, late);
        return ResponseEntity.ok(created);
    }
}
