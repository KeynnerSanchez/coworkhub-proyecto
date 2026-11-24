package com.coworkhub.presentation;

import com.coworkhub.business.service.MemberService;
import com.coworkhub.dto.ConsumptionResponse;
import com.coworkhub.dto.MemberRequest;
import com.coworkhub.dto.MemberResponse;
import com.coworkhub.dto.MemberUpdateRequest;
import com.coworkhub.persistence.enums.MemberStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/miembros")
@Tag(name = "Miembros", description = "Miembros del coworking y su consumo de horas")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Listar miembros",
            description = "Filtros opcionales: ?nombre= (contiene) y ?estado=ACTIVE|SUSPENDED. ADMIN y RECEPTIONIST.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<MemberResponse> listar(@RequestParam(name = "nombre", required = false) String nombre,
                                       @RequestParam(name = "estado", required = false) MemberStatus estado) {
        return memberService.findAll(nombre, estado);
    }

    @Operation(summary = "Ver un miembro", description = "ADMIN y RECEPTIONIST ven a todos. Un MEMBER solo se ve a si mismo.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esMiembroPropio(#id)")
    public MemberResponse obtener(@P("id") @PathVariable("id") Long id) {
        return memberService.findById(id);
    }

    @Operation(summary = "Registrar miembro", description = "Crea el miembro y su usuario con rol MEMBER. ADMIN y RECEPTIONIST.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public MemberResponse crear(@Valid @RequestBody MemberRequest request) {
        return memberService.create(request);
    }

    @Operation(summary = "Actualizar miembro", description = "ADMIN y RECEPTIONIST.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public MemberResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody MemberUpdateRequest request) {
        return memberService.update(id, request);
    }

    @Operation(summary = "Suspender miembro", description = "Un miembro suspendido no puede reservar (RN-05). ADMIN y RECEPTIONIST.")
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public MemberResponse suspender(@PathVariable("id") Long id) {
        return memberService.suspend(id);
    }

    @Operation(summary = "Activar miembro", description = "ADMIN y RECEPTIONIST.")
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public MemberResponse activar(@PathVariable("id") Long id) {
        return memberService.activate(id);
    }

    @Operation(summary = "Eliminar miembro", description = "Solo ADMIN. Falla con 409 (RN-12) si tiene reservas.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable("id") Long id) {
        memberService.delete(id);
    }

    @Operation(summary = "Consumo de horas del mes",
            description = "?mes=yyyy-MM (si no se manda, es el mes actual). Un MEMBER solo ve el suyo.")
    @GetMapping("/{id}/consumo")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @permisos.esMiembroPropio(#id)")
    public ConsumptionResponse consumo(@P("id") @PathVariable("id") Long id,
                                       @RequestParam(name = "mes", required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return memberService.consumption(id, mes);
    }
}
