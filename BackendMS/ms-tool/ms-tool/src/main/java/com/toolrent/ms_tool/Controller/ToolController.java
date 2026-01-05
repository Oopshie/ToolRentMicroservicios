package com.toolrent.ms_tool.Controller;

import com.toolrent.ms_tool.Entity.ToolEntity;
import com.toolrent.ms_tool.Service.ToolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin // Importante para que React (puerto 3000/5173) pueda conectarse
public class ToolController {

    private final ToolService toolService;

    // Inyección por constructor (Best Practice)
    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    // Helper para extraer nombre desde Keycloak
    private String extractEmployeeName(Authentication auth) {
        if (auth == null) return "Sistema"; // Fallback por seguridad

        Jwt jwt = (Jwt) auth.getPrincipal();
        String given = jwt.getClaimAsString("given_name");
        String family = jwt.getClaimAsString("family_name");

        if (given != null && family != null)
            return given + " " + family;

        if (jwt.hasClaim("name"))
            return jwt.getClaimAsString("name");

        return jwt.getClaimAsString("preferred_username");
    }

    // 1. LISTAR TODAS (Frontend: getAll)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/")
    public ResponseEntity<List<ToolEntity>> listTools() {
        return ResponseEntity.ok(toolService.getAllTools());
    }

    // 2. LISTAR DISPONIBLES (Frontend: getAvailable)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/available")
    public ResponseEntity<List<ToolEntity>> getAvailableTools() {
        // Estado 1 = Disponible
        return ResponseEntity.ok(toolService.getToolsByStatus(1));
    }

    // 3. CREAR (Frontend: create)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ToolEntity> addTool(@RequestBody ToolEntity tool, Authentication auth) {
        String employeeName = extractEmployeeName(auth);
        return ResponseEntity.ok(toolService.addTool(tool, employeeName));
    }

    // 4. OBTENER POR ID (Frontend: get)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<ToolEntity> getToolById(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.getToolById(id));
    }

    // 5. ACTUALIZAR (Frontend: update)
    // Contiene la lógica orquestada para precios grupales y movimientos de Kardex
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ToolEntity> updateTool(@PathVariable Long id, @RequestBody ToolEntity tool, Authentication auth) {

        System.out.println("🔧 UPDATE TOOL: ID " + id);

        try {
            ToolEntity existing = toolService.getToolById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            tool.setId(id);
            String employeeName = extractEmployeeName(auth);

            // A. Detección de cambios
            boolean statusChanged = !Objects.equals(existing.getStatus(), tool.getStatus());
            boolean replacementChanged = !Objects.equals(existing.getReplacementValue(), tool.getReplacementValue());

            // B. Si cambió el precio, actualizar GRUPO COMPLETO (Nombre + Categoría)
            if (replacementChanged) {
                System.out.println("💰 Cambio de precio detectado: Actualizando grupo...");
                toolService.updateToolGroupValues(
                        existing.getName(),        // Usamos nombres originales para buscar el grupo
                        existing.getCategory(),
                        tool.getReplacementValue()
                );
            }

            // C. Si cambió el estado, actualizar Kardex (Reparación, Baja, etc.)
            if (statusChanged) {
                System.out.println("🔄 Cambio de estado detectado: " + existing.getStatus() + " -> " + tool.getStatus());
                // Esto llama internamente a KardexService via HTTP
                toolService.updateToolStatus(id, tool.getStatus(), employeeName);
            }

            // D. Actualizar campos descriptivos (Nombre, Categoria, Precio en esta unidad)
            // Nota: updateToolFields NO toca el estatus, así que es seguro llamarlo al final
            ToolEntity saved = toolService.updateToolFields(tool);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 6. BUSQUEDA DUPLICADOS (Frontend: checkDuplicate)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Object>> checkDuplicate(@RequestParam String name, @RequestParam String category) {
        return ResponseEntity.ok(toolService.checkDuplicateAndSuggestPrice(name, category));
    }

    // 7. ELIMINAR FISICO (Frontend: remove)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteToolById(@PathVariable Long id) {
        toolService.deleteToolById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints extra ---

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<List<ToolEntity>> getToolsByName(@RequestParam String name) {
        return ResponseEntity.ok(toolService.getToolsByName(name));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ToolEntity>> getToolByCategory(@PathVariable String category) {
        return ResponseEntity.ok(toolService.getToolsByCategory(category));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}/deactivate")
    public ResponseEntity<ToolEntity> deactivateToolById(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.deactivateTool(id));
    }
}