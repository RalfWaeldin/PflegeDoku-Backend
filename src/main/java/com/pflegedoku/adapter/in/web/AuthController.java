package com.pflegedoku.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pflegedoku.adapter.in.web.dto.AuthResponseDto;
import com.pflegedoku.adapter.in.web.dto.LoginRequestDto;
import com.pflegedoku.config.JwtService;
import com.pflegedoku.core.domain.MitarbeiterStammdaten;
import com.pflegedoku.core.port.MitarbeiterRepository;
import com.pflegedoku.core.service.AuthSessionService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    private final MitarbeiterRepository mitarbeiterRepository;
    private final JwtService jwtService;
    private final AuthSessionService authSessionService; // 1. Service hinzufügen

    public AuthController(MitarbeiterRepository mitarbeiterRepository, 
                          JwtService jwtService, 
                          AuthSessionService authSessionService) { // 2. Injizieren
        this.mitarbeiterRepository = mitarbeiterRepository;
        this.jwtService = jwtService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        var mitarbeiterOpt = mitarbeiterRepository.findById(request.mitarbeiterId());

        if (mitarbeiterOpt.isEmpty() || !mitarbeiterOpt.get().passwort().equals(request.passwort())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ungültige Anmeldedaten");
        }

        MitarbeiterStammdaten mitarbeiter = mitarbeiterOpt.get();
        
        // JWT erzeugen
        String token = jwtService.generateToken(mitarbeiter.mitarbeiterId(), mitarbeiter.rolle());

        // 3. WICHTIG: Session mit dem JWT-Token registrieren
        authSessionService.erstelleSession(token, mitarbeiter);

        return ResponseEntity.ok(new AuthResponseDto(
            token,
            mitarbeiter.mitarbeiterId(),
            mitarbeiter.vorname(),
            mitarbeiter.nachname(),
            mitarbeiter.rolle()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String mitarbeiterId = authentication.getName();
        var mitarbeiterOpt = mitarbeiterRepository.findById(mitarbeiterId);

        if (mitarbeiterOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MitarbeiterStammdaten mitarbeiter = mitarbeiterOpt.get();
        return ResponseEntity.ok(new AuthResponseDto(
            null,
            mitarbeiter.mitarbeiterId(),
            mitarbeiter.vorname(),
            mitarbeiter.nachname(),
            mitarbeiter.rolle()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}


/*
package com.pflegedoku.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pflegedoku.adapter.in.web.dto.AuthResponseDto;
import com.pflegedoku.adapter.in.web.dto.LoginRequestDto;
import com.pflegedoku.config.JwtService;
import com.pflegedoku.core.domain.MitarbeiterStammdaten;
import com.pflegedoku.core.port.MitarbeiterRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    private final MitarbeiterRepository mitarbeiterRepository;
    private final JwtService jwtService;

    public AuthController(MitarbeiterRepository mitarbeiterRepository, JwtService jwtService) {
        this.mitarbeiterRepository = mitarbeiterRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        var mitarbeiterOpt = mitarbeiterRepository.findById(request.mitarbeiterId());

        if (mitarbeiterOpt.isEmpty() || !mitarbeiterOpt.get().passwort().equals(request.passwort())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ungültige Anmeldedaten");
        }

        MitarbeiterStammdaten mitarbeiter = mitarbeiterOpt.get();
        
        // JWT erzeugen statt UUID-Session in Map speichern
        String token = jwtService.generateToken(mitarbeiter.mitarbeiterId(), mitarbeiter.rolle());

        return ResponseEntity.ok(new AuthResponseDto(
            token,
            mitarbeiter.mitarbeiterId(),
            mitarbeiter.vorname(),
            mitarbeiter.nachname(),
            mitarbeiter.rolle()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // authentication.getName() enthält die Mitarbeiter-ID aus dem JWT-Filter
        String mitarbeiterId = authentication.getName();
        var mitarbeiterOpt = mitarbeiterRepository.findById(mitarbeiterId);

        if (mitarbeiterOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MitarbeiterStammdaten mitarbeiter = mitarbeiterOpt.get();
        return ResponseEntity.ok(new AuthResponseDto(
            null, // Token muss bei /me nicht zwingend erneut gesendet werden
            mitarbeiter.mitarbeiterId(),
            mitarbeiter.vorname(),
            mitarbeiter.nachname(),
            mitarbeiter.rolle()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Bei stateless JWT verwirft das Frontend das Token lokal (LocalStorage/SessionStorage).
        // Das Backend muss keine Session auf dem Server löschen.
        return ResponseEntity.ok().build();
    }
}

*/