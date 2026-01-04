package com.toolrent.ms_kardex.Repository;

import com.toolrent.ms_kardex.Entity.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository

public interface KardexRepository extends JpaRepository<KardexEntity, Long> {

    List<KardexEntity> findByMovementType(int type);

    List<KardexEntity> findByToolIdOrderByMovementDateDesc(Long toolId);

    List<KardexEntity> findByMovementDateBetweenOrderByMovementDateDesc(
            String startDate,
            String endDate
    );
}

