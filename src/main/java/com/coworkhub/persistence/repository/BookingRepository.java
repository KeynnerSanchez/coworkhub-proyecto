package com.coworkhub.persistence.repository;

import com.coworkhub.persistence.entity.Booking;
import com.coworkhub.persistence.enums.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Override
    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    Optional<Booking> findById(Long id);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(Long roomId, LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByStatus(BookingStatus status);

    // Reservas de un miembro dentro de un mes (para calcular las horas usadas)
    List<Booking> findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(Long memberId, LocalDateTime desde, LocalDateTime hasta);

    // Cuantas reservas activas hay que se cruzan con el rango (RN-01). Si una termina cuando la otra empieza, NO se cruzan
    @Query("select count(b) from Booking b where b.room.id = :roomId and b.status in :estados "
            + "and b.startTime < :fin and b.endTime > :inicio")
    long countOverlaps(@Param("roomId") Long roomId,
                       @Param("estados") Collection<BookingStatus> estados,
                       @Param("inicio") LocalDateTime inicio,
                       @Param("fin") LocalDateTime fin);

    // Ids de las salas de una sede que estan ocupadas en el rango (para la disponibilidad)
    @Query("select distinct b.room.id from Booking b where b.room.location.id = :locationId and b.status in :estados "
            + "and b.startTime < :fin and b.endTime > :inicio")
    List<Long> findBusyRoomIds(@Param("locationId") Long locationId,
                               @Param("estados") Collection<BookingStatus> estados,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin);

    // Reservas activas futuras de un miembro (RN-06)
    long countByMemberIdAndStatusInAndStartTimeAfter(Long memberId, Collection<BookingStatus> estados, LocalDateTime ahora);

    // Solo el id del dueno de una reserva (lo usa @PreAuthorize)
    @Query("select b.member.id from Booking b where b.id = :id")
    Optional<Long> findMemberIdById(@Param("id") Long id);

    boolean existsByRoomId(Long roomId);

    boolean existsByMemberId(Long memberId);
}
