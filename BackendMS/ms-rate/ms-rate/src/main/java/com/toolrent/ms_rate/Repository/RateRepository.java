package com.toolrent.ms_rate.Repository;

import com.toolrent.ms_rate.Entity.RateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<RateEntity, Long> {

    RateEntity findTopByOrderByIdDesc();
}
