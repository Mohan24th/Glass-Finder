package com.glassfinder.repository;

import com.glassfinder.entity.PhoneModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneModelRepository extends JpaRepository<PhoneModel, Long> {

    Optional<PhoneModel> findByModelNameIgnoreCase(String modelName);
}