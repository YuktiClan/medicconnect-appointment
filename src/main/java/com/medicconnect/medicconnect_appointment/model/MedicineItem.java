package com.medicconnect.medicconnect_appointment.model;

import com.medicconnect.medicconnect_appointment.enums.FrequencyType;
import com.medicconnect.medicconnect_appointment.enums.MealTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "medicine_item")
public class MedicineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String dosage;   // e.g., "500mg"
    private String frequency; // e.g., "2 times a day"
    private Integer duration; // in days

    @Enumerated(EnumType.STRING)
    private MealTime mealTime;

    @Enumerated(EnumType.STRING)
    private FrequencyType frequencyType;


    @ManyToOne
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @Override
    public String toString() {
        return "MedicineItem{id=" + id + "}";
    }

}

