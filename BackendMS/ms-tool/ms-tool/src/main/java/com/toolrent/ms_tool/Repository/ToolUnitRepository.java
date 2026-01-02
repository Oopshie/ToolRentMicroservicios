package com.toolrent.ms_tool.Repository;

import com.toolrent.ms_tool.Entity.ToolUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolUnitRepository extends JpaRepository<ToolUnitEntity, Long> {

    long countByToolIdAndStatus(Long toolId, Integer status);

    List<ToolUnitEntity> findByStatus(Integer status);

    List<ToolUnitEntity> findByToolId(Long toolId);

}
