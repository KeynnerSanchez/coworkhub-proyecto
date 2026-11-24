package com.coworkhub.presentation;

import com.coworkhub.business.service.LocationService;
import com.coworkhub.dto.LocationRequest;
import com.coworkhub.dto.LocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@Tag(name = "Sedes", description = "Sedes del coworking")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "Listar sedes", description = "Cualquier usuario autenticado.")
    @GetMapping
    public List<LocationResponse> listar() {
        return locationService.findAll();
    }

    @Operation(summary = "Ver una sede", description = "Cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public LocationResponse obtener(@PathVariable("id") Long id) {
        return locationService.findById(id);
    }

    @Operation(summary = "Crear sede", description = "Solo ADMIN.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LocationResponse crear(@Valid @RequestBody LocationRequest request) {
        return locationService.create(request);
    }

    @Operation(summary = "Actualizar sede", description = "Solo ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LocationResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody LocationRequest request) {
        return locationService.update(id, request);
    }

    @Operation(summary = "Eliminar sede", description = "Solo ADMIN. Falla con 409 (RN-12) si la sede tiene salas.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        locationService.delete(id);
    }
}
