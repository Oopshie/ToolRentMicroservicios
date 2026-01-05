package com.toolrent.ms_rent.Repository;

import com.toolrent.ms_rent.Entity.RentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository

public interface RentRepository extends JpaRepository<RentEntity, Long> {

    List<RentEntity> findByClientIdAndActiveTrue(Long id);

    boolean existsByClientIdAndToolIdAndActiveTrue(Long id, Long toolId);

}
