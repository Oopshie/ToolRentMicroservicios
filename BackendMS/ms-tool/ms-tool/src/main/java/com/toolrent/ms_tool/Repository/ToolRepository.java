package com.toolrent.ms_tool.Repository;

import com.toolrent.ms_tool.Entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface ToolRepository extends JpaRepository<ToolEntity, Long> {

    public ToolEntity findByName(String name);

    List<ToolEntity> findByStatus(int status);

    List<ToolEntity> findByCategory(String category);

    List<ToolEntity> findByNameContainingIgnoreCase(String namePart);

    List<ToolEntity> findByNameAndCategory(String name, String category);

}

