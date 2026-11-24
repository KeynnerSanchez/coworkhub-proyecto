package com.coworkhub.presentation;

import com.coworkhub.business.service.MembershipPlanService;
import com.coworkhub.dto.MembershipPlanRequest;
import com.coworkhub.dto.MembershipPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes", description = "Planes de membresia")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @Operation(summary = "Listar planes", description = "Cualquier usuario autenticado.")
    @GetMapping
    public List<MembershipPlanResponse> listar() {
        return planService.findAll();
    }

    @Operation(summary = "Ver un plan", description = "Cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public MembershipPlanResponse obtener(@PathVariable("id") Long id) {
        return planService.findById(id);
    }

    @Operation(summary = "Crear plan", description = "Solo ADMIN. El nombre es unico (RN-11).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public MembershipPlanResponse crear(@Valid @RequestBody MembershipPlanRequest request) {
        return planService.create(request);
    }

    @Operation(summary = "Actualizar plan", description = "Solo ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MembershipPlanResponse actualizar(@PathVariable("id") Long id,
                                             @Valid @RequestBody MembershipPlanRequest request) {
        return planService.update(id, request);
    }

    @Operation(summary = "Eliminar plan", description = "Solo ADMIN. Falla con 409 (RN-12) si tiene miembros.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        planService.delete(id);
    }
}
