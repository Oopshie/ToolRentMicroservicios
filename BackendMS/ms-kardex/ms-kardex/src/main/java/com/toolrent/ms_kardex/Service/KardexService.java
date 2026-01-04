package com.toolrent.ms_kardex.Service;

import com.toolrent.ms_kardex.Model.KardexDTO;
import com.toolrent.ms_kardex.Entity.KardexEntity;
import com.toolrent.ms_kardex.Repository.KardexRepository;
import com.toolrent.ms_kardex.Model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private RestTemplate restTemplate;

    private KardexDTO toDTO(KardexEntity k) {

        String toolName = "unknown Tool";
        String toolCategory = "unknown Category";

        try {
            if (k.getToolId() != null) {
                Tool tool = restTemplate.getForObject("http://ms-tool-service/api/tools/" + k.getToolId(), Tool.class);
                if (tool != null) {
                    toolName = tool.getName();
                    toolCategory = tool.getCategory();
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching tool data for kardex ID "+ k.getId() + ": " + e.getMessage());
        }

        return new KardexDTO(
                k.getId(),
                k.getMovementDate(),
                k.getMovementType(),
                k.getQuantity(),
                k.getToolId(),
                toolName,
                toolCategory,
                k.getEmployeeName()
        );
    }

    public void registerMovement(int type, Long toolId, String employeeName) {
        KardexEntity mov = new KardexEntity();
        mov.setMovementType(type);
        mov.setMovementDate(LocalDateTime.now().toString());
        mov.setQuantity(1);
        mov.setToolId(toolId);
        mov.setEmployeeName(employeeName);

        kardexRepository.save(mov);
    }


    public List<KardexDTO> getByTool(Long toolId) {
        return kardexRepository.findByToolIdOrderByMovementDateDesc(toolId)
                .stream().map(this::toDTO).toList();
    }

    public List<KardexDTO> getByDateRange(String from, String to) {
        return kardexRepository.findByMovementDateBetweenOrderByMovementDateDesc(from, to)
                .stream().map(this::toDTO).toList();
    }

    public List<KardexDTO> getAllMovements() {
        return kardexRepository.findAll()
                .stream().map(this::toDTO).toList();
    }
}
