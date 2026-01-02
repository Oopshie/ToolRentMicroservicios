package com.toolrent.ms_tool.Service;

import com.toolrent.ms_tool.Entity.ToolEntity;
import com.toolrent.ms_tool.Entity.ToolUnitEntity;
import com.toolrent.ms_tool.Repository.ToolRepository;
import com.toolrent.ms_tool.Repository.ToolUnitRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolUnitRepository toolUnitRepository;

    public ToolService(ToolRepository toolRepository,
                       ToolUnitRepository toolUnitRepository) {
        this.toolRepository = toolRepository;
        this.toolUnitRepository = toolUnitRepository;
    }

    // ====== LISTADOS ======

    public List<ToolEntity> getAllTools() {
        return toolRepository.findAll();
    }

    public List<ToolEntity> getToolsByName(String name) {
        return toolRepository.findByNameContainingIgnoreCase(name);
    }

    public List<ToolEntity> getToolsByCategory(String category) {
        return toolRepository.findByCategory(category.toLowerCase());
    }

    public ToolEntity getToolById(Long id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Herramienta no encontrada"));
    }

    // ====== CREAR HERRAMIENTA + UNIDADES ======

    @Transactional
    public ToolEntity createTool(String name, String category,
                           Integer replacementValue, int quantity) {

        ToolEntity tool = new ToolEntity();
        tool.setName(name.toLowerCase().trim());
        tool.setCategory(category.toLowerCase().trim());
        tool.setReplacementValue(replacementValue);
        tool.setActive(true);

        ToolEntity saved = toolRepository.save(tool);

        List<ToolUnitEntity> units = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            ToolUnitEntity u = new ToolUnitEntity();
            u.setTool(saved);
            u.setStatus(1); // Disponible
            units.add(u);
        }

        toolUnitRepository.saveAll(units);
        return saved;
    }

    // ====== ACTUALIZAR DATOS DEL PRODUCTO ======

    @Transactional
    public ToolEntity updateTool(Long id, ToolEntity updated) {
        ToolEntity existing = getToolById(id);

        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setReplacementValue(updated.getReplacementValue());

        return toolRepository.save(existing);
    }

    // ====== STOCK ======

    public long getAvailableStock(Long toolId) {
        return toolUnitRepository.countByToolIdAndStatus(toolId, 1);
    }

    // ====== DUPLICADO ======

    public Optional<ToolEntity> checkDuplicate(String name, String category) {
        return toolRepository.findByNameAndCategory(
                name.toLowerCase().trim(),
                category.toLowerCase().trim()
        );
    }
}

