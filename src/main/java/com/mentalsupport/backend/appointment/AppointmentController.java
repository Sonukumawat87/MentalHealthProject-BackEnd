package com.mentalsupport.backend.appointment;

import com.mentalsupport.backend.therapist.Therapist;
import com.mentalsupport.backend.therapist.TherapistAvailabilityRepository;
import com.mentalsupport.backend.therapist.TherapistRepository;
import com.mentalsupport.backend.user.User;
import com.mentalsupport.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final TherapistRepository therapistRepository;
    private final TherapistAvailabilityRepository availabilityRepository;

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            TherapistRepository therapistRepository,
            TherapistAvailabilityRepository availabilityRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.therapistRepository = therapistRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @PostMapping
    public ResponseEntity<?> book(Authentication authentication, @RequestBody Map<String, Object> body) {
        User student = userRepository.findByEmail(authentication.getName()).orElseThrow();

        Long therapistId = Long.valueOf(String.valueOf(body.get("therapistId")));
        String date = String.valueOf(body.get("date"));
        String timeSlot = String.valueOf(body.get("timeSlot"));

        if (appointmentRepository.findByTherapistIdAndDateAndTimeSlot(therapistId, date, timeSlot).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "This slot is already booked"));
        }

        Appointment appointment = new Appointment();
        appointment.setStudentId(student.getId());
        appointment.setTherapistId(therapistId);
        appointment.setDate(date);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus("confirmed");

        Appointment saved = appointmentRepository.save(appointment);
        availabilityRepository.deleteByTherapistIdAndDateAndTimeSlot(therapistId, date, timeSlot);

        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myAppointments(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();

        List<Appointment> appointments = "therapist".equals(currentUser.getRole().name())
                ? appointmentRepository.findByTherapistId(
                        therapistRepository.findByEmail(currentUser.getEmail()).orElseThrow().getId()
                  )
                : appointmentRepository.findByStudentId(currentUser.getId());

        return ResponseEntity.ok(appointments.stream().map(this::toResponse).toList());
    }

    @GetMapping
    public ResponseEntity<?> allAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll().stream().map(this::toResponse).toList());
    }

    private Map<String, Object> toResponse(Appointment appointment) {
        User student = userRepository.findById(appointment.getStudentId()).orElseThrow();
        Therapist therapist = therapistRepository.findById(appointment.getTherapistId()).orElseThrow();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", appointment.getId());
        response.put("date", appointment.getDate());
        response.put("timeSlot", appointment.getTimeSlot());
        response.put("status", appointment.getStatus());
        response.put("student", Map.of("name", student.getName(), "email", student.getEmail()));
        response.put("therapist", Map.of("name", therapist.getName(), "email", therapist.getEmail()));
        return response;
    }
}