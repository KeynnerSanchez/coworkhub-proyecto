package com.coworkhub;

import com.coworkhub.business.calculo.CalculadoraCostosEstandar;
import com.coworkhub.business.calculo.ConsumoHoras;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.service.impl.BookingServiceImpl;
import com.coworkhub.dto.BookingRequest;
import com.coworkhub.dto.BookingResponse;
import com.coworkhub.persistence.entity.Booking;
import com.coworkhub.persistence.entity.Location;
import com.coworkhub.persistence.entity.Member;
import com.coworkhub.persistence.entity.MembershipPlan;
import com.coworkhub.persistence.entity.Room;
import com.coworkhub.persistence.enums.BookingStatus;
import com.coworkhub.persistence.enums.MemberStatus;
import com.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Pruebas unitarias de las reglas de negocio mas importantes (sin base de datos, con Mockito)
class BookingServiceImplTest {

    // Hora fija: 3 de junio de 2030, 13:00
    private final Clock reloj = Clock.fixed(Instant.parse("2030-06-03T13:00:00Z"), ZoneId.of("UTC"));
    private final LocalDateTime ahora = LocalDateTime.of(2030, 6, 3, 13, 0);

    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private MemberRepository memberRepository;
    private BookingServiceImpl servicio;

    private Room sala;
    private Member miembro;

    @BeforeEach
    void preparar() {
        bookingRepository = mock(BookingRepository.class);
        roomRepository = mock(RoomRepository.class);
        memberRepository = mock(MemberRepository.class);
        servicio = new BookingServiceImpl(bookingRepository, roomRepository, memberRepository,
                mock(AdditionalServiceRepository.class), new CalculadoraCostosEstandar(50),
                mock(ConsumoHoras.class), reloj);

        Location sede = new Location();
        sede.setId(1L);
        sede.setName("Sede");
        sede.setOpeningTime(LocalTime.of(6, 0));
        sede.setClosingTime(LocalTime.of(22, 0));

        sala = new Room();
        sala.setId(1L);
        sala.setName("Sala Andes");
        sala.setCapacity(10);
        sala.setHourlyRate(new BigDecimal("40000"));
        sala.setActive(true);
        sala.setLocation(sede);

        MembershipPlan plan = new MembershipPlan();
        plan.setIncludedHoursPerMonth(5);
        plan.setOverageDiscount(10);
        plan.setMaxActiveBookings(2);

        miembro = new Member();
        miembro.setId(1L);
        miembro.setFullName("Ana Gomez");
        miembro.setStatus(MemberStatus.ACTIVE);
        miembro.setPlan(plan);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Booking reservaQueEmpiezaEn(LocalDateTime inicio, BookingStatus estado) {
        Booking b = new Booking();
        b.setId(1L);
        b.setRoom(sala);
        b.setMember(miembro);
        b.setStartTime(inicio);
        b.setEndTime(inicio.plusHours(2));
        b.setStatus(estado);
        b.setRoomCost(new BigDecimal("70000.00"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        return b;
    }

    @Test
    void cancelarConMasDe24HorasNoCobra() {
        reservaQueEmpiezaEn(ahora.plusHours(30), BookingStatus.PENDING);
        BookingResponse r = servicio.cancel(1L);
        assertEquals(BookingStatus.CANCELLED, r.status());
        assertEquals(0, r.cancellationFee().compareTo(BigDecimal.ZERO));
    }

    @Test
    void cancelarEntre24Y2HorasCobraElCincuentaPorCiento() {
        reservaQueEmpiezaEn(ahora.plusHours(5), BookingStatus.CONFIRMED);
        BookingResponse r = servicio.cancel(1L);
        assertEquals(BookingStatus.CANCELLED, r.status());
        assertEquals(new BigDecimal("35000.00"), r.cancellationFee());
    }

    @Test
    void cancelarConMenosDe2HorasDaRN08() {
        reservaQueEmpiezaEn(ahora.plusHours(1), BookingStatus.PENDING);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.cancel(1L));
        assertEquals("RN-08", e.getCodigo());
    }

    @Test
    void confirmarUnaReservaCanceladaDaRN09() {
        reservaQueEmpiezaEn(ahora.plusHours(30), BookingStatus.CANCELLED);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.confirm(1L));
        assertEquals("RN-09", e.getCodigo());
    }

    @Test
    void reservarConDuracionDe45MinutosDaRN02() {
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(miembro));
        LocalDateTime inicio = LocalDateTime.of(2030, 6, 4, 10, 0);
        BookingRequest pedido = new BookingRequest(1L, 1L, inicio, inicio.plusMinutes(45), 3, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.create(pedido));
        assertEquals("RN-02", e.getCodigo());
    }

    @Test
    void reservarFueraDelHorarioDeLaSedeDaRN03() {
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(miembro));
        LocalDateTime inicio = LocalDateTime.of(2030, 6, 4, 21, 30);
        BookingRequest pedido = new BookingRequest(1L, 1L, inicio, inicio.plusHours(1), 3, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.create(pedido));
        assertEquals("RN-03", e.getCodigo());
    }

    @Test
    void reservarConUnMiembroSuspendidoDaRN05() {
        miembro.setStatus(MemberStatus.SUSPENDED);
        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(miembro));
        LocalDateTime inicio = LocalDateTime.of(2030, 6, 4, 10, 0);
        BookingRequest pedido = new BookingRequest(1L, 1L, inicio, inicio.plusHours(1), 3, null);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.create(pedido));
        assertEquals("RN-05", e.getCodigo());
    }
}
