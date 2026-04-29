package com.mentalsupport.backend.therapist;

import jakarta.persistence.*;

@Entity
@Table(name = "therapist_availability")
public class TherapistAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long therapistId;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String timeSlot;

    public TherapistAvailability() {}

    public Long getId() { return id; }
    public Long getTherapistId() { return therapistId; }
    public void setTherapistId(Long therapistId) { this.therapistId = therapistId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}