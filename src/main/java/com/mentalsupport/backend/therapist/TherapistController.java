package com.mentalsupport.backend.therapist;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/therapists")
public class TherapistController {

    private final TherapistRepository therapistRepository;
    private final TherapistAvailabilityRepository availabilityRepository;

    public TherapistController(
            TherapistRepository therapistRepository,
            TherapistAvailabilityRepository availabilityRepository
    ) {
        this.therapistRepository = therapistRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Map<String, Object>> data = therapistRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Therapist therapist) {
        Therapist saved = therapistRepository.save(therapist);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMine(Authentication authentication) {
        Therapist therapist = therapistRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(toResponse(therapist));
    }

    @PostMapping("/me/availability")
    public ResponseEntity<?> addAvailability(
            Authentication authentication,
            @RequestBody Map<String, Object> body
    ) {
        Therapist therapist = therapistRepository.findByEmail(authentication.getName()).orElseThrow();

        String date = String.valueOf(body.get("date"));
        List<?> slots = (List<?>) body.get("timeSlots");

        for (Object slotObj : slots) {
            String slot = String.valueOf(slotObj);
            boolean exists = availabilityRepository.findByTherapistId(therapist.getId())
                    .stream()
                    .anyMatch(a -> a.getDate().equals(date) && a.getTimeSlot().equals(slot));

            if (!exists) {
                TherapistAvailability availability = new TherapistAvailability();
                availability.setTherapistId(therapist.getId());
                availability.setDate(date);
                availability.setTimeSlot(slot);
                availabilityRepository.save(availability);
            }
        }

        return ResponseEntity.ok(toResponse(therapist));
    }

    private Map<String, Object> toResponse(Therapist therapist) {
        List<TherapistAvailability> availability = availabilityRepository.findByTherapistId(therapist.getId());

        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (TherapistAvailability item : availability) {
            grouped.computeIfAbsent(item.getDate(), key -> new ArrayList<>()).add(item.getTimeSlot());
        }

        List<Map<String, Object>> availableSlots = grouped.entrySet().stream()
                .map(entry -> Map.of(
                        "date", entry.getKey(),
                        "timeSlots", entry.getValue()
                ))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", therapist.getId());
        response.put("name", therapist.getName());
        response.put("specialization", therapist.getSpecialization());
        response.put("bio", therapist.getBio());
        response.put("email", therapist.getEmail());
        response.put("availableSlots", availableSlots);
        return response;
    }
}