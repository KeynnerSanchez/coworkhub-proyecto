package com.coworkhub.business.calculo;

import com.coworkhub.persistence.entity.Booking;
import com.coworkhub.persistence.enums.BookingStatus;
import com.coworkhub.persistence.repository.BookingRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

// Calcula cuantas horas de su plan ha usado un miembro en un mes (RN-07)
@Component
public class ConsumoHoras {

    private final BookingRepository bookingRepository;

    public ConsumoHoras(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public BigDecimal horasUsadas(Long memberId, YearMonth mes) {
        LocalDateTime desde = mes.atDay(1).atStartOfDay();
        LocalDateTime hasta = mes.plusMonths(1).atDay(1).atStartOfDay();
        List<Booking> reservas = bookingRepository
                .findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(memberId, desde, hasta);

        long minutos = reservas.stream()
                .filter(this::cuentaComoUsada)
                .mapToLong(b -> Duration.between(b.getStartTime(), b.getEndTime()).toMinutes())
                .sum();
        return aHoras(minutos);
    }

    // Una reserva cancelada con 24 h o mas devuelve las horas (su cargo es 0).
    // Si se cancelo tarde (cargo mayor que 0) las horas siguen contando como usadas.
    private boolean cuentaComoUsada(Booking b) {
        if (b.getStatus() != BookingStatus.CANCELLED) {
            return true;
        }
        return b.getCancellationFee() != null && b.getCancellationFee().signum() > 0;
    }

    public static BigDecimal aHoras(long minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
