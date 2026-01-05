package com.toolrent.ms_report.Service;

import com.toolrent.ms_report.DTO.ActiveRentReportDTO;
import com.toolrent.ms_report.DTO.LateClientReportDTO;
import com.toolrent.ms_report.DTO.ToolRankingReportDTO;
import com.toolrent.ms_report.Model.Rent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private RestTemplate restTemplate;

    //=============================================================
    // 1. REPORTE DE ARRIENDOS ACTIVOS
    //=============================================================
    public List<ActiveRentReportDTO> getActiveRents(){
        // Traer arriendos (que ya vienen con nombres desde ms-rent)
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        List<ActiveRentReportDTO> report = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Filtramos y mapeamos
        Arrays.stream(allRents)
                .filter(Rent::isActive)
                .forEach(rent -> {
                    boolean isLate = false;
                    try {
                        if (rent.getFinishDate() != null) {
                            isLate = LocalDate.parse(rent.getFinishDate()).isBefore(today);
                        }
                    } catch (Exception e) { /* ignorar error fecha */ }

                    report.add(new ActiveRentReportDTO(
                            rent.getId(),
                            // ✅ USAMOS LOS NOMBRES QUE YA VIENEN EN EL JSON
                            rent.getClientName() != null ? rent.getClientName() : "Desconocido",
                            rent.getToolName() != null ? rent.getToolName() : "Desconocida",
                            rent.getStartDate(),
                            rent.getFinishDate(),
                            isLate
                    ));
                });

        return report;
    }

    //=============================================================
    // 2. REPORTE DE CLIENTES MOROSOS
    //=============================================================
    public List<LateClientReportDTO> getLateClients() {
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        LocalDate today = LocalDate.now();
        Map<Long, List<Rent>> rentsByClient = new HashMap<>();

        // Identificar morosos
        for (Rent rent : allRents) {
            try {
                if (rent.getFinishDate() == null) continue;
                LocalDate finish = LocalDate.parse(rent.getFinishDate());
                boolean isLate = false;

                if (rent.isActive()) {
                    if (finish.isBefore(today)) isLate = true;
                } else if (rent.getReturnDate() != null) {
                    if (LocalDate.parse(rent.getReturnDate()).isAfter(finish)) isLate = true;
                }

                if (isLate) {
                    rentsByClient.computeIfAbsent(rent.getClientId(), k -> new ArrayList<>()).add(rent);
                }
            } catch (Exception e) { continue; }
        }

        List<LateClientReportDTO> report = new ArrayList<>();

        for (Map.Entry<Long, List<Rent>> entry : rentsByClient.entrySet()) {
            Long clientId = entry.getKey();
            List<Rent> lateRents = entry.getValue();

            // Usamos el nombre del PRIMER arriendo de la lista (ya viene desde ms-rent)
            String clientName = "Desconocido";
            if (!lateRents.isEmpty() && lateRents.get(0).getClientName() != null) {
                clientName = lateRents.get(0).getClientName();
            }

            long totalLateDays = 0;
            long occurrences = 0;

            for (Rent r : lateRents) {
                try {
                    LocalDate finish = LocalDate.parse(r.getFinishDate());
                    LocalDate end = r.isActive() ? today : LocalDate.parse(r.getReturnDate());
                    long days = ChronoUnit.DAYS.between(finish, end);
                    if (days > 0) {
                        totalLateDays += days;
                        occurrences++;
                    }
                } catch (Exception e) {}
            }

            report.add(new LateClientReportDTO(
                    clientId,
                    clientName, // ✅ Nombre directo
                    "Ver Detalle", // El RUT no viene en ms-rent, ponemos texto genérico para no romper
                    totalLateDays,
                    occurrences
            ));
        }
        return report;
    }

    //=============================================================
    // 3. RANKING
    //=============================================================
    public List<ToolRankingReportDTO> getToolRanking() {
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        // Agrupar por NOMBRE DE HERRAMIENTA directamente
        Map<String, Long> counts = Arrays.stream(allRents)
                .filter(r -> r.getToolName() != null)
                .collect(Collectors.groupingBy(
                        Rent::getToolName, // Agrupamos por el nombre que ya viene
                        Collectors.counting()
                ));

        List<ToolRankingReportDTO> report = new ArrayList<>();
        counts.forEach((name, count) -> {
            report.add(new ToolRankingReportDTO(
                    name,
                    "General", // Categoría no viene en Rent, usamos genérico o la sacas de otro lado si urge
                    count
            ));
        });

        report.sort((a, b) -> Long.compare(b.rentalCount, a.rentalCount));
        return report;
    }
}
