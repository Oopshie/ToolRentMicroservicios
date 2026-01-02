package com.toolrent.ms_tool.Service;


import com.toolrent.ms_tool.Entity.ToolUnitEntity;
import com.toolrent.ms_tool.Repository.ToolUnitRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolUnitService {

    private final ToolUnitRepository toolUnitRepository;

    public ToolUnitService(ToolUnitRepository toolUnitRepository) {
        this.toolUnitRepository = toolUnitRepository;
    }

    public List<ToolUnitEntity> getAvailableUnits() {
        return toolUnitRepository.findByStatus(1);
    }

    public ToolUnitEntity getUnitById(Long id) {
        return toolUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));
    }

    @Transactional
    public ToolUnitEntity updateUnitStatus(Long unitId, int newStatus) {
        ToolUnitEntity unit = getUnitById(unitId);
        unit.setStatus(newStatus);
        return toolUnitRepository.save(unit);
    }

    @Transactional
    public ToolUnitEntity deactivateUnit(Long unitId) {
        return updateUnitStatus(unitId, 4);
    }
}
