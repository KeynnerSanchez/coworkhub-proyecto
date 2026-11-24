package com.coworkhub.persistence.repository;

import com.coworkhub.persistence.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
