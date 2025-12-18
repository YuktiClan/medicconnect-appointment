package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false)
    private String givenName;

    @Column
    private String middleName;

    @Column(nullable = false)
    private String familyName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column
    private LocalDate birthdate;

    @Column(nullable = false)
    private boolean birthdateEstimated = false;

    @Column
    private String primaryContactNumber;

    @Column
    private String primaryEmail;

    @Column
    private String governmentId; // e.g., Aadhaar or national id

    @Column(columnDefinition = "text")
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    private String pincode;

    @Column
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
