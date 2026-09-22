package com.glassfinder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "glass_compatibility",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"box_id", "model_id"})
        }
)
@Getter
@Setter
public class GlassCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private GlassBox box;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private PhoneModel model;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}