package com.mentalsupport.backend.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStudentId(Long studentId);
    List<Appointment> findByTherapistId(Long therapistId);
    Optional<Appointment> findByTherapistIdAndDateAndTimeSlot(Long therapistId, String date, String timeSlot);
}