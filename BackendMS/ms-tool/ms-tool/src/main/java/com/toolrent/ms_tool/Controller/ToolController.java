package com.toolrent.ms_tool.Controller;

import com.toolrent.ms_tool.DTO.CreateToolRequest;
import com.toolrent.ms_tool.Entity.ToolEntity;
import com.toolrent.ms_tool.Service.ToolService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "http://localhost:5173")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ToolEntity createTool(@RequestBody CreateToolRequest req) {
        return toolService.createTool(
                req.getName(),
                req.getCategory(),
                req.getReplacementValue(),
                req.getQuantity()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping
    public List<ToolEntity> listTools() {
        return toolService.getAllTools();
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/search")
    public List<ToolEntity> search(@RequestParam String name) {
        return toolService.getToolsByName(name);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/category/{category}")
    public List<ToolEntity> byCategory(@PathVariable String category) {
        return toolService.getToolsByCategory(category);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/{id}")
    public ToolEntity getById(@PathVariable Long id) {
        return toolService.getToolById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/{id}/stock")
    public long stock(@PathVariable Long id) {
        return toolService.getAvailableStock(id);
    }
}

