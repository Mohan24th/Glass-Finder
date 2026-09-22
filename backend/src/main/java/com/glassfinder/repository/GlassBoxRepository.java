package com.glassfinder.repository;

import com.glassfinder.entity.GlassBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlassBoxRepository extends JpaRepository<GlassBox, Long> {

    Optional<GlassBox> findByBoxCode(String boxCode);
}