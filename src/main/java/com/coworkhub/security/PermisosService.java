package com.coworkhub.security;

import com.coworkhub.persistence.repository.BookingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Se usa dentro de @PreAuthorize: @PreAuthorize("... or @permisos.esMiembroPropio(#id)")
// Regla: ADMIN y RECEPTIONIST pueden ver todo. Un MEMBER solo lo que es suyo (RN-13)
@Component("permisos")
public class PermisosService {

    private final BookingRepository bookingRepository;

    public PermisosService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private UsuarioAutenticado actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        return null;
    }

    // true si el miembroId es el del usuario que hace la peticion (o si es personal del coworking)
    public boolean esMiembroPropio(Long miembroId) {
        UsuarioAutenticado usuario = actual();
        if (usuario == null) {
            return false;
        }
        if (!"MEMBER".equals(usuario.getRole())) {
            return true;
        }
        return usuario.getMiembroId() != null && usuario.getMiembroId().equals(miembroId);
    }

    // true si la reserva es del usuario. Si la reserva no existe devuelve true para que el servicio responda 404
    public boolean esReservaPropia(Long reservaId) {
        UsuarioAutenticado usuario = actual();
        if (usuario == null) {
            return false;
        }
        if (!"MEMBER".equals(usuario.getRole())) {
            return true;
        }
        Optional<Long> duenoId = bookingRepository.findMemberIdById(reservaId);
        return duenoId.isEmpty() || duenoId.get().equals(usuario.getMiembroId());
    }
}
