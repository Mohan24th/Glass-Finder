package com.glassfinder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_models")
@Getter
@Setter
public class PhoneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", unique = true, nullable = false, length = 100)
    private String modelName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}