package com.hospital;

import com.hospital.model.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for appointment entity rules, setter validation, and status transitions.
 */
public class AppointmentRulesTest {

    @Test
    @DisplayName("Should create scheduled appointment with normalized status")
    public void testAppointmentCreation() {
        Appointment appt = new Appointment(1, 2, LocalDate.now().plusDays(2), LocalTime.of(10, 0));
        Assertions.assertEquals(Appointment.STATUS_SCHEDULED, appt.getStatus());
        Assertions.assertEquals(1, appt.getPatientId());
        Assertions.assertEquals(2, appt.getDoctorId());
    }

    @Test
    @DisplayName("Should accept valid statuses: Scheduled, Completed, Cancelled")
    public void testValidStatusTransitions() {
        Appointment appt = new Appointment();
        appt.setStatus("Completed");
        Assertions.assertEquals(Appointment.STATUS_COMPLETED, appt.getStatus());

        appt.setStatus("Cancelled");
        Assertions.assertEquals(Appointment.STATUS_CANCELLED, appt.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid appointment statuses")
    public void testInvalidStatus() {
        Appointment appt = new Appointment();
        Assertions.assertThrows(IllegalArgumentException.class, () -> appt.setStatus("Postponed"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> appt.setStatus(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> appt.setStatus(null));
    }

    @Test
    @DisplayName("Should format date and time string properly")
    public void testFormattedDateTime() {
        LocalDate date = LocalDate.of(2026, 11, 20);
        LocalTime time = LocalTime.of(14, 30);
        Appointment appt = new Appointment(1, 1, date, time);

        Assertions.assertEquals("2026-11-20 14:30", appt.getFormattedDateTime());
    }
}
