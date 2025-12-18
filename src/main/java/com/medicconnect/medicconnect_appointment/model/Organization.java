package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String uuid; // UUID string

    @Column(nullable=false)
    private OrganizationType organizationType;

    @Column(nullable=false)
    private String legalName;

    @Column
    private String registrationNumber;

    @Column
    private Integer establishmentYear;

    @Enumerated(EnumType.STRING)
    private OwnershipType ownershipType;

    @Column
    private String licenseNumber;

    @Column
    private Long parentOrganizationId;

    @Column
    private String website;

    @Column
    private String category;

    // Contact
    @Column
    private String contactEmail;

    @Column
    private String contactMobile;

    @Column
    private String contactLandline;

    @Column
    private String billingEmail;

    @Column
    private String emergencyContact;

    // Location
    @Column(columnDefinition = "text")
    private String address;

    @Column
    private String country;

    @Column
    private String state;

    @Column
    private String city;

    @Column
    private String pincode;

    @Column
    private String geoCoordinates; // "lat,lng"

    // Flags
    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean mobileVerified = false;

    @Column(nullable = false)
    private boolean documentsUploaded = false;

    @Column(nullable = false)
    private boolean documentsVerified = false;

    @Column(nullable=false)
    private boolean active = true;

    @Column
    private String createdBy;

    @Column
    private Instant createdAt;
}
