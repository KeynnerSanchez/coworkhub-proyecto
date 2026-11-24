package com.coworkhub.presentation;

import com.coworkhub.business.exception.SolicitudInvalidaException;
import com.coworkhub.business.service.BookingService;
import com.coworkhub.dto.BookingItemRequest;
import com.coworkhub.dto.BookingRequest;
import com.coworkhub.dto.BookingResponse;
import com.coworkhub.persistence.enums.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Crear, consultar, confirmar, cancelar y completar reservas")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Crear reserva",
            description = "ADMIN y RECEPTIONIST reservan para cualquiera. Un MEMBER solo a su nombre. Valida RN-01 a RN-07.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esMiembroPropio(#request.miembroId())")
    public BookingResponse crear(@P("request") @Valid @RequestBody BookingRequest request) {
        return bookingService.create(request);
    }

    @Operation(summary = "Listar reservas",
            description = "Un solo filtro: ?miembroId= | ?salaId=&fecha=yyyy-MM-dd | ?estado=. "
                    + "Un MEMBER solo puede usar su propio miembroId.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esMiembroPropio(#miembroId)")
    public List<BookingResponse> listar(
            @P("miembroId") @RequestParam(name = "miembroId", required = false) Long miembroId,
            @RequestParam(name = "salaId", required = false) Long salaId,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "estado", required = false) BookingStatus estado) {
        if (miembroId != null) {
            return bookingService.findByMember(miembroId);
        }
        if (salaId != null && fecha != null) {
            return bookingService.findByRoomAndDate(salaId, fecha);
        }
        if (estado != null) {
            return bookingService.findByStatus(estado);
        }
        throw new SolicitudInvalidaException("Use uno de estos filtros: miembroId, salaId con fecha, o estado");
    }

    @Operation(summary = "Ver una reserva", description = "ADMIN y RECEPTIONIST ven cualquiera. Un MEMBER solo las suyas.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esReservaPropia(#id)")
    public BookingResponse obtener(@P("id") @PathVariable("id") Long id) {
        return bookingService.findById(id);
    }

    @Operation(summary = "Agregar un servicio adicional",
            description = "Solo si la reserva esta PENDING (RN-10). ADMIN, RECEPTIONIST o el dueno.")
    @PostMapping("/{id}/servicios")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esReservaPropia(#id)")
    public BookingResponse agregarServicio(@P("id") @PathVariable("id") Long id,
                                           @Valid @RequestBody BookingItemRequest request) {
        return bookingService.addService(id, request);
    }

    @Operation(summary = "Quitar un servicio adicional",
            description = "Solo si la reserva esta PENDING (RN-10). ADMIN, RECEPTIONIST o el dueno.")
    @DeleteMapping("/{id}/servicios/{servicioId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esReservaPropia(#id)")
    public BookingResponse quitarServicio(@P("id") @PathVariable("id") Long id,
                                          @PathVariable("servicioId") Long servicioId) {
        return bookingService.removeService(id, servicioId);
    }

    @Operation(summary = "Confirmar reserva", description = "PENDING pasa a CONFIRMED (RN-09). ADMIN y RECEPTIONIST.")
    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public BookingResponse confirmar(@PathVariable("id") Long id) {
        return bookingService.confirm(id);
    }

    @Operation(summary = "Cancelar reserva",
            description = "24 h o mas: sin cargo. Entre 24 h y 2 h: cargo del 50 % de la sala. Menos de 2 h: 409 (RN-08). "
                    + "ADMIN, RECEPTIONIST o el dueno.")
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esReservaPropia(#id)")
    public BookingResponse cancelar(@P("id") @PathVariable("id") Long id) {
        return bookingService.cancel(id);
    }

    @Operation(summary = "Completar reserva", description = "CONFIRMED pasa a COMPLETED (RN-09). ADMIN y RECEPTIONIST.")
    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public BookingResponse completar(@PathVariable("id") Long id) {
        return bookingService.complete(id);
    }
}
