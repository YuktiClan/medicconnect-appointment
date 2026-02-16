package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class NotesRequest {
    private String patientComments;
    private String doctorNotes;
}

