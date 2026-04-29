package com.mentalsupport.backend.therapist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapistAvailabilityRepository extends JpaRepository<TherapistAvailability, Long> {
    List<TherapistAvailability> findByTherapistId(Long therapistId);
    void deleteByTherapistIdAndDateAndTimeSlot(Long therapistId, String date, String timeSlot);
}