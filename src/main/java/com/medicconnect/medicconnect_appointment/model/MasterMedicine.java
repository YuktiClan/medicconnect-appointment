package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_medicines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}

