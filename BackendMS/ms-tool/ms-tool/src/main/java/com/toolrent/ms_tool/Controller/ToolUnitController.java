package com.toolrent.ms_tool.Controller;

import com.toolrent.ms_tool.Entity.ToolUnitEntity;
import com.toolrent.ms_tool.Service.ToolUnitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "http://localhost:5173")
public class ToolUnitController {

    private final ToolUnitService toolUnitService;

    public ToolUnitController(ToolUnitService toolUnitService) {
        this.toolUnitService = toolUnitService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/available")
    public List<ToolUnitEntity> getAvailableUnits() {
        return toolUnitService.getAvailableUnits();
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @PutMapping("/units/{unitId}/status/{status}")
    public ToolUnitEntity updateStatus(@PathVariable Long unitId,
                                 @PathVariable int status) {
        return toolUnitService.updateUnitStatus(unitId, status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/units/{unitId}")
    public ToolUnitEntity deactivate(@PathVariable Long unitId) {
        return toolUnitService.deactivateUnit(unitId);
    }
}
