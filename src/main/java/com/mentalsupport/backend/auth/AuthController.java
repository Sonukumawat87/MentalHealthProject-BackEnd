package com.mentalsupport.backend.auth;

import com.mentalsupport.backend.auth.dto.AuthResponse;
import com.mentalsupport.backend.auth.dto.LoginRequest;
import com.mentalsupport.backend.auth.dto.RegisterRequest;
import com.mentalsupport.backend.security.JwtService;
import com.mentalsupport.backend.therapist.Therapist;
import com.mentalsupport.backend.therapist.TherapistRepository;
import com.mentalsupport.backend.user.Role;
import com.mentalsupport.backend.user.User;
import com.mentalsupport.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TherapistRepository therapistRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TherapistRepository therapistRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.therapistRepository = therapistRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String name = req.name().trim();
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        String password = req.password().trim();
        String roleValue = req.role().trim();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        Role role;
        try {
            role = Role.valueOf(roleValue.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);

        if (role == Role.therapist) {
            Therapist therapist = new Therapist();
            therapist.setName(name);
            therapist.setEmail(email);
            therapist.setSpecialization(req.specialization() == null ? "" : req.specialization().trim());
            therapist.setBio(req.bio() == null ? "" : req.bio().trim());
            therapistRepository.save(therapist);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name().toLowerCase(Locale.ROOT))
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getRole().name().toLowerCase(Locale.ROOT),
                        user.getName(),
                        user.getEmail()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name().toLowerCase(Locale.ROOT))
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getRole().name().toLowerCase(Locale.ROOT),
                        user.getName(),
                        user.getEmail()
                )
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/therapist-user")
    public ResponseEntity<?> createTherapistUser(@RequestBody Map<String, Object> body) {
        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase(Locale.ROOT);
        String password = String.valueOf(body.getOrDefault("password", "")).trim();
        String specialization = String.valueOf(body.getOrDefault("specialization", "")).trim();
        String bio = String.valueOf(body.getOrDefault("bio", "")).trim();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.therapist);
        userRepository.save(user);

        Therapist therapist = new Therapist();
        therapist.setName(name);
        therapist.setEmail(email);
        therapist.setSpecialization(specialization);
        therapist.setBio(bio);
        therapistRepository.save(therapist);

        return ResponseEntity.ok(Map.of("message", "Therapist user created successfully"));
    }
}