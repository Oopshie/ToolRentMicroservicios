package com.toolrent.ms_report.Service;

import com.toolrent.ms_report.DTO.ActiveRentReportDTO;
import com.toolrent.ms_report.DTO.ToolRankingReportDTO;
import com.toolrent.ms_report.DTO.LateClientReportDTO;
import com.toolrent.ms_report.Model.Client;
import com.toolrent.ms_report.Model.Rent;
import com.toolrent.ms_report.Model.Tool;
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
    // Reporte de arriendos activos
    //=============================================================
    public List<ActiveRentReportDTO> getActiveRents(){

        //Traer arriendos de ms-rent
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rents/all", Rent[].class);
        if (allRents == null) {
            return new ArrayList<>();
        }

        // Filtrar solo los arriendos activos
        List<Rent> activeRents = Arrays.stream(allRents)
                .filter(Rent::isActive)
                .toList();

        List<ActiveRentReportDTO> report = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Rent rent : activeRents){

            //obtener nombres de cliente y herramienta desde ms-client y ms-tool
            String clientName = getClientName(rent.getClientId());
            String toolName = getToolName(rent.getToolId());

            //calcular si está atrasado
            boolean isLate = LocalDate.parse(rent.getFinishDate()).isBefore(today);

            //Llenar DTO
            report.add(new ActiveRentReportDTO(
                    rent.getId(),
                    clientName,
                    toolName,
                    rent.getStartDate(),
                    rent.getFinishDate(),
                    isLate
            ));
        }
        return report;
    }

    //=============================================================
    // Reporte de clientes morosos
    //=============================================================

    public List<LateClientReportDTO> getLateClients() {
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rents/all", Rent[].class);
        if (allRents == null)
            return new ArrayList<>();

        LocalDate today = LocalDate.now();
        Map<Long, List <Rent>> rentsByClient = new HashMap<>();

        // identificar arriendos atrasados
        for (Rent rent : allRents) {
            LocalDate finishDate = LocalDate.parse(rent.getFinishDate());
            boolean isLate = false;
            long daysLate = 0;

            if(rent.isActive()) {
                //activo y vencido
                if (finishDate.isBefore(today)) {
                    isLate = true;
                    daysLate = ChronoUnit.DAYS.between(finishDate, today);
                }
            }else if(rent.getReturnDate() != null){
                //devuelto pero vencido
                LocalDate returnDate = LocalDate.parse(rent.getReturnDate());
                if(returnDate.isAfter(finishDate)){
                    isLate = true;
                    daysLate = ChronoUnit.DAYS.between(finishDate, returnDate);
                }
            }
                
            //si está atrasado, lo agregamos al mapa para sumar después
            if (isLate){
                rentsByClient.computeIfAbsent(rent.getClientId(), k -> new ArrayList<>()).add(rent);
            }
        }

        List<LateClientReportDTO> report = new ArrayList<>();

        // agrupar por cliente y sumar días de atraso
        for (Map.Entry<Long, List<Rent>> entry : rentsByClient.entrySet()){
            Long clientId = entry.getKey();
            List<Rent> lateRents = entry.getValue();

            // calcular totales
            long totalLateDays = 0;
            long occurrences = 0;

            for (Rent r: lateRents){
                LocalDate finishDate = LocalDate.parse(r.getFinishDate());
                LocalDate endCalculationDate = r.isActive() ? today : LocalDate.parse(r.getReturnDate());

                long days = ChronoUnit.DAYS.between(finishDate, endCalculationDate);
                if (days > 0){
                    totalLateDays += days;
                    occurrences++;
                }
            }

            // obtener datos del cliente
            Client client = getClientFull(clientId);
            String clientName = (client != null) ? client.getName() : "Desconocido";
            String clientRut = (client != null) ? client.getRut() : "Desconocido";

            // llenar DTO
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
    // Reporte de ranking de herramientas
    //=============================================================
    public List<ToolRankingReportDTO> getToolRanking() {

        // obtener todos los arriendos
        Rent[] allRents = restTemplate.getForObject("http://ms-rent-service/api/rents/all", Rent[].class);

        // obtener todas las herramientas
        Tool[] allTools = restTemplate.getForObject("http://ms-tool-service/api/tools/all", Tool[].class);

        if (allRents == null || allTools == null)
            return new ArrayList<>();

        Map<Long, Tool> toolsMap = Arrays.stream(allTools)
                .collect(Collectors.toMap(Tool::getId, tool -> tool));

        Map<String, Long> groupCounts = new HashMap<>();
        Map<String, Tool> groupInfo = new HashMap<>();

        for (Rent rent : allRents) {
            // encontrar la herramienta correspondiente
            Tool tool = toolsMap.get(rent.getToolId());
            if (tool != null){
                // clave única por nombre+categoría (ignorando mayúsculas y espacios)
                String key = tool.getName().trim().toLowerCase() + "|" + tool.getCategory().trim().toLowerCase();

                // Sumar al contador
                groupCounts.put(key, groupCounts.getOrDefault(key, 0L) + 1);

                // Guardar referencia de la herramienta para sacar nombre bonito después
                groupInfo.putIfAbsent(key, tool);
            }
        }

        // construir el DTO

        List<ToolRankingReportDTO> report = new ArrayList<>();

        for (Map.Entry<String, Long> entry : groupCounts.entrySet()){
            String key = entry.getKey();
            Long count = entry.getValue();
            Tool info = groupInfo.get(key);

            report.add(new ToolRankingReportDTO(
                    info.getName(),
                    info.getCategory(),
                    count
            ));
        }

        // ordenar por cantidad descendente
        report.sort((a, b) -> Long.compare(b.rentalCount, a.rentalCount));

        return report;
    }

    // ======================== MÉTODOS AUXILIARES PRIVADOS ========================

    private String getClientName(Long id) {
        try {
            // Usamos la URL completa como pediste
            Client c = restTemplate.getForObject("http://ms-client-service/api/clients/" + id, Client.class);
            return c != null ? c.getName() : "Desconocido";
        } catch (Exception e) {
            return "Error al obtener cliente";
        }
    }

    private String getToolName(Long id) {
        try {
            Tool t = restTemplate.getForObject("http://ms-tool-service/api/tools/" + id, Tool.class);
            return t != null ? t.getName() : "Desconocido";
        } catch (Exception e) {
            return "Error al obtener herramienta";
        }
    }

    // Este lo usas en el reporte de morosos y ranking para traer el objeto completo
    private Client getClientFull(Long id) {
        try {
            return restTemplate.getForObject("http://ms-client-service/api/clients/" + id, Client.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Tool getToolFull(Long id) {
        try {
            return restTemplate.getForObject("http://ms-tool-service/api/tools/" + id, Tool.class);
        } catch (Exception e) {
            return null;
        }
    }
}
