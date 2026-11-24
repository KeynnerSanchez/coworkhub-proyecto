package com.coworkhub.presentation;

import com.coworkhub.business.service.AdditionalServiceService;
import com.coworkhub.dto.AdditionalServiceRequest;
import com.coworkhub.dto.AdditionalServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios-adicionales")
@Tag(name = "Servicios adicionales", description = "Cafe, impresiones, catering...")
public class AdditionalServiceController {

    private final AdditionalServiceService serviceService;

    public AdditionalServiceController(AdditionalServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @Operation(summary = "Listar servicios adicionales", description = "Cualquier usuario autenticado.")
    @GetMapping
    public List<AdditionalServiceResponse> listar() {
        return serviceService.findAll();
    }

    @Operation(summary = "Ver un servicio adicional", description = "Cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public AdditionalServiceResponse obtener(@PathVariable("id") Long id) {
        return serviceService.findById(id);
    }

    @Operation(summary = "Crear servicio adicional", description = "Solo ADMIN. El nombre es unico (RN-11).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AdditionalServiceResponse crear(@Valid @RequestBody AdditionalServiceRequest request) {
        return serviceService.create(request);
    }

    @Operation(summary = "Actualizar servicio adicional", description = "Solo ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdditionalServiceResponse actualizar(@PathVariable("id") Long id,
                                                @Valid @RequestBody AdditionalServiceRequest request) {
        return serviceService.update(id, request);
    }

    @Operation(summary = "Eliminar servicio adicional", description = "Solo ADMIN. Falla con 409 (RN-12) si esta en reservas.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        serviceService.delete(id);
    }
}
