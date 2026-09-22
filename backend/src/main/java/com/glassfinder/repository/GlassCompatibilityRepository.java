package com.glassfinder.repository;

import com.glassfinder.entity.GlassCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlassCompatibilityRepository
        extends JpaRepository<GlassCompatibility, Long> {

    List<GlassCompatibility> findByModel_ModelNameIgnoreCase(String modelName);

    List<GlassCompatibility> findByBox_Id(Long boxId);
}