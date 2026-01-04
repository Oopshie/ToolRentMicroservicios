package com.toolrent.ms_rent.Service;


import com.toolrent.ms_rent.Model.*;
import com.toolrent.ms_rent.Repository.*;
import com.toolrent.ms_rent.Entity.RentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class RentService {

    @Autowired private RentRepository rentRepository;
    @Autowired private RestTemplate restTemplate;

    public Object createRent(String clientRut, Long toolId, String finishDate, String employeeName) {

        // ===== CLIENTE =====
        Client client;
        try {
            client = restTemplate.getForObject("http://ms-client-service/api/clients/getByRut/" + clientRut, Client.class);
        } catch (Exception e) {
            return "CLIENT_NOT_FOUND";
        }

        if (client.getStatus() != 1)
            return "CLIENT_RESTRICTED";

        // ===== PRÉSTAMOS ACTIVOS =====
        List<RentEntity> activeRents = rentRepository.findByClientIdAndActiveTrue(client.getId());
        if (activeRents.size() >= 5)
            return "CLIENT_MAX_RENTS";

        if (rentRepository.existsByClientIdAndToolIdAndActiveTrue(client.getId(), toolId))
            return "TOOL_ALREADY_RENTED_BY_CLIENT";

        // ===== HERRAMIENTA =====
        Tool tool;
            try {
                tool = restTemplate.getForObject("http://ms-tool-service/api/tools/" +toolId,  Tool.class);
            } catch (Exception e) {
                return "TOOL_NOT_FOUND";
            }

        if (tool.getStatus() != 1)
            return "TOOL_NOT_AVAILABLE";

        // ===== FECHA =====
        LocalDate end = LocalDate.parse(finishDate);
        if (end.isBefore(LocalDate.now()))
            return "Fecha de devolución inválida";

        // ===== CREAR PRÉSTAMO =====
        RentEntity rent = new RentEntity();
        rent.setClientId(client.getId());
        rent.setToolId(toolId);
        rent.setStartDate(LocalDate.now().toString());
        rent.setFinishDate(finishDate);

        rent.setActive(true);
        rent.setReturnDate(null);
        rent.setFineAmount(0);
        rent.setDamaged(false);
        rent.setIrreparable(false);

        rent.setEmployeeName(employeeName); // EMPLEADO DESDE KEYCLOAK

        rentRepository.save(rent);

        // ===== ACTUALIZAR ESTADO TOOL =====
        tool.setStatus(2);
        restTemplate.put("http://ms-tool-service/api/tools/" + tool.getId(), tool);

        // ===== KARDEX =====
        Kardex mov = new Kardex();
        mov.setMovementType(1); // 1 = préstamo
        mov.setMovementDate(LocalDateTime.now().toString());
        mov.setQuantity(1);
        mov.setToolId(toolId);
        mov.setEmployeeName(employeeName);

        restTemplate.postForObject("http://ms-kardex-service/api/kardex/internal", mov, Void.class);

        return rent;
    }

    public List<RentDTO> getAllRentsOrdered() {

        List<RentEntity> rents = rentRepository.findAll();
        LocalDate today = LocalDate.now();

        List<RentDTO> dtos = rents.stream()
                .map(r -> {
                    String clientName = "Desconocido";
                    String toolName = "Desconocido";
                    try {
                        Client client = restTemplate.getForObject("http://ms-client-service/api/clients/" + r.getClientId(), Client.class);
                        if (client != null)
                            clientName = client.getName();
                        Tool tool = restTemplate.getForObject("http://ms-tool-service/api/tools/" + r.getToolId(), Tool.class);
                        if (tool != null)
                            toolName = tool.getName();
                    } catch (Exception e) {
                        System.out.println("Error fetching external names: " + e.getMessage());
                    }
                    return new RentDTO(
                        r.getId(),
                        clientName,
                        toolName,
                        r.getStartDate(),
                        r.getFinishDate(),
                        r.getReturnDate(),
                        r.isActive(),
                        r.isDamaged(),
                        r.isIrreparable(),
                        r.getFineAmount(),
                        r.getTotalAmount(),
                        r.getEmployeeName()
                );
                })
                .collect(Collectors.toList());


        return dtos.stream()
                .sorted((a, b) -> {
                    boolean aLate = a.getReturnDate() == null &&
                            LocalDate.parse(a.getFinishDate()).isBefore(today);

                    boolean bLate = b.getReturnDate() == null &&
                            LocalDate.parse(b.getFinishDate()).isBefore(today);

                    if (aLate && !bLate) return -1;
                    if (!aLate && bLate) return 1;

                    if (a.isActive() && !b.isActive()) return -1;
                    if (!a.isActive() && b.isActive()) return 1;

                    return 0;
                })
                .collect(Collectors.toList());
    }

    public RentDTO returnTool(Long rentId, boolean damaged, boolean irreparable) {

        RentEntity rent = rentRepository.findById(rentId)
                .orElseThrow(() -> new RuntimeException("Rent not found"));

        Tool tool = restTemplate.getForObject("http://ms-tool-service/api/tools/" + rent.getToolId(), Tool.class);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        Rate rate = restTemplate.getForObject("http://ms-rate-service/api/rates/latest", Rate.class);
        if (rate == null) {
            throw new RuntimeException("Rate missing");
        }

        // Actualizar estados
        rent.setReturnDate(LocalDate.now().toString());
        rent.setActive(false);
        rent.setDamaged(damaged);
        rent.setIrreparable(irreparable);

        // Calcular total
        int total = calculateTotal(rent, tool, rate);
        rent.setTotalAmount(total);

        // Guardar cambios
        rentRepository.save(rent);

        int newToolStatus = 1; // Disponible
        int kardexMovementType = 2; // devolución

        // Actualizar herramienta
        // Actualizar estado de la herramienta según daño
        if (irreparable) {
            newToolStatus = 4; // Dado de baja
            kardexMovementType = 3; // baja
        } else if (damaged) {
            newToolStatus = 3; // En reparación
            kardexMovementType = 5; // en reparación
        }

        tool.setStatus(newToolStatus);
        restTemplate.put("http://ms-tool-service/api/tools/" + tool.getId(), tool);


        // Kardex
        Kardex mov = new Kardex();
        mov.setMovementType(2); // devolución
        mov.setMovementDate(LocalDateTime.now().toString());
        mov.setQuantity(1);
        mov.setToolId(tool.getId());
        mov.setEmployeeName(rent.getEmployeeName());

        restTemplate.postForObject("http://ms-kardex-service/api/kardex/internal", mov, Void.class);

        // Devolver DTO
        return new RentDTO(
                rent.getId(),
                restTemplate.getForObject("http://ms-client-service/api/clients/" + rent.getClientId(), Client.class).getName(),
                tool.getName(),
                rent.getStartDate(),
                rent.getFinishDate(),
                rent.getReturnDate(),
                rent.isActive(),
                rent.isDamaged(),
                rent.isIrreparable(),
                rent.getFineAmount(),
                rent.getTotalAmount(),
                rent.getEmployeeName()
        );
    }

    private int calculateTotal(RentEntity rent, Tool tool, Rate rate) {

        LocalDate start = LocalDate.parse(rent.getStartDate());
        LocalDate finish = LocalDate.parse(rent.getFinishDate());
        LocalDate returned = LocalDate.parse(rent.getReturnDate());

        // Días de arriendo (incluyendo el día inicial)
        int rentalDays = (int) (finish.toEpochDay() - start.toEpochDay()) + 1;
        if (rentalDays < 1) rentalDays = 1;

        int rentalCost = rentalDays * rate.getDailyRentalRate();

        // Días de atraso
        int lateDays = 0;
        if (returned.isAfter(finish)) {
            lateDays = (int) (returned.toEpochDay() - finish.toEpochDay());
        }

        int lateFee = lateDays * rate.getDailyLateFeeRent();

        // Reposición
        int replacementCost = 0;
        if (rent.isIrreparable()) {
            replacementCost = tool.getReplacementValue();
        }

        return rentalCost + lateFee + replacementCost;
    }

    public List<RentEntity>getAll() {
        return rentRepository.findAll();
    }

}

