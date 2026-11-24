package com.coworkhub.persistence.repository;

import com.coworkhub.persistence.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // EntityGraph: trae sede y equipamiento en la misma consulta (evita el problema N+1)
    @Override
    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findAll();

    @Override
    @EntityGraph(attributePaths = {"location", "equipment"})
    Optional<Room> findById(Long id);

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationId(Long locationId);

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationIdAndActiveTrue(Long locationId);

    // Bloqueo pesimista: select ... for update. Dos reservas a la vez sobre la misma sala hacen fila
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    boolean existsByLocationId(Long locationId);

    boolean existsByLocationIdAndName(Long locationId, String name);

    boolean existsByLocationIdAndNameAndIdNot(Long locationId, String name, Long id);

    boolean existsByEquipmentId(Long equipmentId);
}
