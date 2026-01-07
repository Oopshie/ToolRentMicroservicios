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
    // 1. REPORTE DE ARRIENDOS ACTIVOS (FILTRADO)
    //=============================================================
    public List<ActiveRentReportDTO> getActiveRents(String fromStr, String toStr){
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        // 1. Convertir fechas (si son nulas, usamos rango infinito)
        LocalDate fromDate = (fromStr == null || fromStr.isEmpty()) ? LocalDate.MIN : LocalDate.parse(fromStr);
        LocalDate toDate = (toStr == null || toStr.isEmpty()) ? LocalDate.MAX : LocalDate.parse(toStr);

        List<ActiveRentReportDTO> report = new ArrayList<>();
        LocalDate today = LocalDate.now();

        Arrays.stream(allRents)
                .filter(Rent::isActive) // Primero filtramos que esté activo
                .filter(rent -> {       // LUEGO FILTRAMOS POR FECHA DE INICIO
                    try {
                        LocalDate start = LocalDate.parse(rent.getStartDate());
                        // Si está fuera del rango, lo descartamos
                        return !start.isBefore(fromDate) && !start.isAfter(toDate);
                    } catch (Exception e) { return false; }
                })
                .forEach(rent -> {
                    boolean isLate = false;
                    try {
                        if (rent.getFinishDate() != null) {
                            isLate = LocalDate.parse(rent.getFinishDate()).isBefore(today);
                        }
                    } catch (Exception e) { /* ignorar error fecha */ }

                    report.add(new ActiveRentReportDTO(
                            rent.getId(),
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
    // 2. REPORTE DE CLIENTES MOROSOS (FILTRADO)
    //=============================================================
    public List<LateClientReportDTO> getLateClients(String fromStr, String toStr) {
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        LocalDate today = LocalDate.now();
        Map<Long, List<Rent>> rentsByClient = new HashMap<>();

        // 1. Convertir fechas usando las variables que recibimos
        LocalDate fromDate = (fromStr == null || fromStr.isEmpty()) ? LocalDate.MIN : LocalDate.parse(fromStr);
        LocalDate toDate = (toStr == null || toStr.isEmpty()) ? LocalDate.MAX : LocalDate.parse(toStr);

        // Identificar morosos
        for (Rent rent : allRents) {
            try {
                if (rent.getFinishDate() == null) continue;
                LocalDate finish = LocalDate.parse(rent.getFinishDate());

                // --- FILTRO DE FECHAS ---
                // Si la fecha de término está fuera del rango, saltamos este arriendo
                if (finish.isBefore(fromDate) || finish.isAfter(toDate)) {
                    continue;
                }

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

            String clientName = "Desconocido";
            if (!lateRents.isEmpty() && lateRents.get(0).getClientName() != null) {
                clientName = lateRents.get(0).getClientName();
            }

            String clientRut = "Sin RUT";
            try {
                Map clientData = restTemplate.getForObject("http://ms-client-service/api/client/" + clientId, Map.class);
                if (clientData != null && clientData.get("rut") != null) {
                    clientRut = clientData.get("rut").toString();
                }
            } catch (Exception e) {
                System.out.println("No se pudo obtener RUT para cliente " + clientId);
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
                    clientName,
                    clientRut,
                    totalLateDays,
                    occurrences
            ));
        }
        return report;
    }

    //=============================================================
    // 3. RANKING (FILTRADO)
    //=============================================================
    public List<ToolRankingReportDTO> getToolRanking(String fromStr, String toStr) {
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rent/all", Rent[].class);
        if (allRents == null) return new ArrayList<>();

        // 1. Convertir fechas
        LocalDate fromDate = (fromStr == null || fromStr.isEmpty()) ? LocalDate.MIN : LocalDate.parse(fromStr);
        LocalDate toDate = (toStr == null || toStr.isEmpty()) ? LocalDate.MAX : LocalDate.parse(toStr);

        // Agrupar por NOMBRE DE HERRAMIENTA directamente
        Map<String, Long> counts = Arrays.stream(allRents)
                .filter(r -> r.getToolName() != null)
                .filter(r -> { // --- FILTRO DE FECHAS ---
                    try {
                        LocalDate start = LocalDate.parse(r.getStartDate());
                        return !start.isBefore(fromDate) && !start.isAfter(toDate);
                    } catch (Exception e) { return false; }
                })
                .collect(Collectors.groupingBy(
                        Rent::getToolName,
                        Collectors.counting()
                ));

        List<ToolRankingReportDTO> report = new ArrayList<>();
        counts.forEach((name, count) -> {
            report.add(new ToolRankingReportDTO(
                    name,
                    "General",
                    count
            ));
        });

        report.sort((a, b) -> Long.compare(b.rentalCount, a.rentalCount));
        return report;
    }
}