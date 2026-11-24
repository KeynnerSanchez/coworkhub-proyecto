package com.coworkhub.business.service.impl;

import com.coworkhub.business.calculo.CalculadoraCostos;
import com.coworkhub.business.calculo.ConsumoHoras;
import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.exception.SolicitudInvalidaException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.BookingService;
import com.coworkhub.dto.BookingItemRequest;
import com.coworkhub.dto.BookingRequest;
import com.coworkhub.dto.BookingResponse;
import com.coworkhub.persistence.entity.*;
import com.coworkhub.persistence.enums.BookingStatus;
import com.coworkhub.persistence.enums.MemberStatus;
import com.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    // Estados que "ocupan" la sala
    private static final List<BookingStatus> ACTIVOS = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final AdditionalServiceRepository serviceRepository;
    private final CalculadoraCostos calculadora;
    private final ConsumoHoras consumoHoras;
    private final Clock clock;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository,
                              MemberRepository memberRepository, AdditionalServiceRepository serviceRepository,
                              @Qualifier("calculadoraEstandar") CalculadoraCostos calculadora,
                              ConsumoHoras consumoHoras, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
        this.calculadora = calculadora;
        this.consumoHoras = consumoHoras;
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Crear reserva
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public BookingResponse create(BookingRequest request) {
        // 1) Lo PRIMERO es bloquear la fila de la sala (select ... for update).
        //    Si llegan dos reservas a la vez para la misma sala, la segunda espera aqui
        //    hasta que la primera termine, y entonces si ve el cruce (RN-01).
        Room room = roomRepository.findByIdForUpdate(request.salaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala no encontrada con id " + request.salaId()));
        Member member = memberRepository.findById(request.miembroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro no encontrado con id " + request.miembroId()));

        LocalDateTime inicio = request.startTime();
        LocalDateTime fin = request.endTime();
        LocalDateTime ahora = LocalDateTime.now(clock);

        // RN-05: el miembro debe estar activo
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new ReglaNegocioException("RN-05", "El miembro esta suspendido y no puede reservar");
        }
        // RN-04: sala activa y con capacidad suficiente
        if (!room.isActive()) {
            throw new ReglaNegocioException("RN-04", "La sala no esta activa");
        }
        if (request.attendees() > room.getCapacity()) {
            throw new ReglaNegocioException("RN-04", "Los asistentes superan la capacidad de la sala");
        }
        // RN-02: duracion entre 30 min y 8 h, en multiplos de 30 min, el mismo dia
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReglaNegocioException("RN-02", "La reserva debe empezar y terminar el mismo dia");
        }
        if (minutos < 30 || minutos > 480 || minutos % 30 != 0) {
            throw new ReglaNegocioException("RN-02",
                    "La duracion debe estar entre 30 minutos y 8 horas, en multiplos de 30 minutos");
        }
        // RN-03: dentro del horario de la sede, en el futuro y con 1 hora de anticipacion
        Location sede = room.getLocation();
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        if (horaInicio.isBefore(sede.getOpeningTime()) || horaFin.isAfter(sede.getClosingTime())) {
            throw new ReglaNegocioException("RN-03", "La reserva esta fuera del horario de la sede");
        }
        if (!inicio.isAfter(ahora)) {
            throw new ReglaNegocioException("RN-03", "La reserva debe ser en el futuro");
        }
        if (Duration.between(ahora, inicio).toMinutes() < 60) {
            throw new ReglaNegocioException("RN-03", "La reserva debe hacerse con al menos 1 hora de anticipacion");
        }
        // RN-01: no se puede cruzar con otra reserva activa de la misma sala
        if (bookingRepository.countOverlaps(room.getId(), ACTIVOS, inicio, fin) > 0) {
            throw new ReglaNegocioException("RN-01", "La sala ya esta reservada en ese horario");
        }
        // RN-06: maximo de reservas activas futuras segun el plan
        MembershipPlan plan = member.getPlan();
        long activas = bookingRepository.countByMemberIdAndStatusInAndStartTimeAfter(member.getId(), ACTIVOS, ahora);
        if (activas >= plan.getMaxActiveBookings()) {
            throw new ReglaNegocioException("RN-06", "El miembro alcanzo el maximo de reservas activas de su plan");
        }

        // RN-07: costo de la sala. Solo se cobran las horas que pasan de las incluidas en el plan
        BigDecimal horasReserva = ConsumoHoras.aHoras(minutos);
        BigDecimal usadas = consumoHoras.horasUsadas(member.getId(), YearMonth.from(inicio));
        BigDecimal incluidas = BigDecimal.valueOf(plan.getIncludedHoursPerMonth());
        BigDecimal libres = incluidas.subtract(usadas).max(BigDecimal.ZERO);
        BigDecimal excedentes = horasReserva.subtract(libres).max(BigDecimal.ZERO);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setMember(member);
        booking.setStartTime(inicio);
        booking.setEndTime(fin);
        booking.setAttendees(request.attendees());
        booking.setStatus(BookingStatus.PENDING);
        booking.setRoomCost(calculadora.costoSala(excedentes, room.getHourlyRate(), plan.getOverageDiscount()));
        booking.setCancellationFee(BigDecimal.ZERO);

        if (request.services() != null) {
            for (BookingItemRequest linea : request.services()) {
                agregarLinea(booking, linea);
            }
        }
        recalcularTotales(booking);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByMember(Long miembroId) {
        if (!memberRepository.existsById(miembroId)) {
            throw new RecursoNoEncontradoException("Miembro no encontrado con id " + miembroId);
        }
        return bookingRepository.findByMemberId(miembroId).stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByRoomAndDate(Long salaId, LocalDate fecha) {
        if (!roomRepository.existsById(salaId)) {
            throw new RecursoNoEncontradoException("Sala no encontrada con id " + salaId);
        }
        return bookingRepository
                .findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(salaId,
                        fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay())
                .stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByStatus(BookingStatus estado) {
        return bookingRepository.findByStatus(estado).stream().map(Mapeador::toResponse).toList();
    }

    // ------------------------------------------------------------------
    // Servicios adicionales (RF-06 y RN-10)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public BookingResponse addService(Long reservaId, BookingItemRequest request) {
        Booking booking = buscar(reservaId);
        validarPendiente(booking);
        agregarLinea(booking, request);
        recalcularTotales(booking);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse removeService(Long reservaId, Long servicioId) {
        Booking booking = buscar(reservaId);
        validarPendiente(booking);
        BookingItem linea = booking.getItems().stream()
                .filter(i -> i.getService().getId().equals(servicioId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no tiene ese servicio"));
        booking.getItems().remove(linea); // orphanRemoval borra la linea
        recalcularTotales(booking);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    // ------------------------------------------------------------------
    // Estados (RN-09)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public BookingResponse confirm(Long id) {
        Booking booking = buscar(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ReglaNegocioException("RN-09", "Solo se puede confirmar una reserva PENDING");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse complete(Long id) {
        Booking booking = buscar(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ReglaNegocioException("RN-09", "Solo se puede completar una reserva CONFIRMED");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    // RN-08: cancelacion segun la anticipacion
    @Override
    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = buscar(id);
        if (!ACTIVOS.contains(booking.getStatus())) {
            throw new ReglaNegocioException("RN-09", "Solo se puede cancelar una reserva PENDING o CONFIRMED");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        long minutosFaltantes = Duration.between(ahora, booking.getStartTime()).toMinutes();

        if (!booking.getStartTime().isAfter(ahora) || minutosFaltantes < 120) {
            throw new ReglaNegocioException("RN-08", "No se puede cancelar con menos de 2 horas de anticipacion");
        }
        BigDecimal cargo;
        if (minutosFaltantes >= 24 * 60) {
            cargo = BigDecimal.ZERO; // 24 h o mas: sin cargo
        } else {
            cargo = calculadora.cargoCancelacion(booking.getRoomCost()); // entre 24 h y 2 h: 50 %
        }
        booking.setCancellationFee(cargo);
        booking.setStatus(BookingStatus.CANCELLED);
        return Mapeador.toResponse(bookingRepository.save(booking));
    }

    // ------------------------------------------------------------------
    // Metodos privados de ayuda
    // ------------------------------------------------------------------
    private Booking buscar(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id " + id));
    }

    private void validarPendiente(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ReglaNegocioException("RN-10", "Solo se pueden cambiar los servicios de una reserva PENDING");
        }
    }

    // Agrega una linea de servicio. Si el servicio ya estaba, suma la cantidad
    private void agregarLinea(Booking booking, BookingItemRequest request) {
        if (request.quantity() == null || request.quantity() < 1) {
            throw new SolicitudInvalidaException("La cantidad debe ser al menos 1");
        }
        AdditionalService servicio = serviceRepository.findById(request.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Servicio adicional no encontrado con id " + request.servicioId()));

        BookingItem existente = booking.getItems().stream()
                .filter(i -> i.getService().getId().equals(servicio.getId()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            existente.setQuantity(existente.getQuantity() + request.quantity());
            return;
        }
        BookingItem linea = new BookingItem();
        linea.setBooking(booking);
        linea.setService(servicio);
        linea.setQuantity(request.quantity());
        linea.setAppliedUnitPrice(servicio.getUnitPrice()); // se congela el precio de hoy
        booking.getItems().add(linea);
    }

    private void recalcularTotales(Booking booking) {
        BigDecimal costoServicios = booking.getItems().stream()
                .map(i -> i.getAppliedUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setServicesCost(costoServicios);
        booking.setTotalCost(booking.getRoomCost().add(costoServicios));
    }
}
