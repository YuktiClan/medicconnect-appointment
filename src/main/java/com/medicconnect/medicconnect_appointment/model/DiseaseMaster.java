package com.medicconnect.medicconnect_appointment.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disease_master")
public class DiseaseMaster {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public DiseaseMaster() {
    }

    public DiseaseMaster(String code, String description, String category) {
        this.code = code;
        this.description = description;
        this.category = category;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}