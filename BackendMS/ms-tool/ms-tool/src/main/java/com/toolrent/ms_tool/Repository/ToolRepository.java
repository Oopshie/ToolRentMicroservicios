package com.toolrent.ms_tool.Repository;

import com.toolrent.ms_tool.Entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToolRepository extends JpaRepository<ToolEntity, Long> {

    List<ToolEntity> findByNameContainingIgnoreCase(String name);
    List<ToolEntity> findByCategory(String category);
    Optional<ToolEntity> findByNameAndCategory(String name, String category);

}

