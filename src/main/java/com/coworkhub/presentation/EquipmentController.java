package com.coworkhub.presentation;

import com.coworkhub.business.service.EquipmentService;
import com.coworkhub.dto.EquipmentRequest;
import com.coworkhub.dto.EquipmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamientos")
@Tag(name = "Equipamientos", description = "Proyector, pizarra, etc.")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @Operation(summary = "Listar equipamientos", description = "Cualquier usuario autenticado.")
    @GetMapping
    public List<EquipmentResponse> listar() {
        return equipmentService.findAll();
    }

    @Operation(summary = "Ver un equipamiento", description = "Cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public EquipmentResponse obtener(@PathVariable("id") Long id) {
        return equipmentService.findById(id);
    }

    @Operation(summary = "Crear equipamiento", description = "Solo ADMIN. El nombre es unico (RN-11).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EquipmentResponse crear(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.create(request);
    }

    @Operation(summary = "Actualizar equipamiento", description = "Solo ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EquipmentResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @Operation(summary = "Eliminar equipamiento", description = "Solo ADMIN. Falla con 409 (RN-12) si alguna sala lo usa.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        equipmentService.delete(id);
    }
}
