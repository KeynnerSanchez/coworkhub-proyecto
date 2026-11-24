package com.coworkhub.presentation;

import com.coworkhub.business.service.RoomService;
import com.coworkhub.dto.RoomRequest;
import com.coworkhub.dto.RoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
@Tag(name = "Salas", description = "Salas, oficinas y puestos")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "Listar salas", description = "Opcional: ?sedeId= para filtrar por sede. Cualquier usuario autenticado.")
    @GetMapping
    public List<RoomResponse> listar(@RequestParam(name = "sedeId", required = false) Long sedeId) {
        return roomService.findAll(sedeId);
    }

    @Operation(summary = "Ver una sala", description = "Cualquier usuario autenticado.")
    @GetMapping("/{id}")
    public RoomResponse obtener(@PathVariable("id") Long id) {
        return roomService.findById(id);
    }

    @Operation(summary = "Crear sala", description = "Solo ADMIN. El nombre no se repite dentro de la misma sede (RN-11).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse crear(@Valid @RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @Operation(summary = "Actualizar sala", description = "Solo ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @Operation(summary = "Eliminar sala", description = "Solo ADMIN. Falla con 409 (RN-12) si la sala tiene reservas.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        roomService.delete(id);
    }
}
