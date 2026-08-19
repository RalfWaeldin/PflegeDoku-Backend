package com.pflegedoku.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pflegedoku.adapter.in.web.dto.AnalyseRequestDto;
import com.pflegedoku.core.domain.MitarbeiterStammdaten;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.service.AuthSessionService;
import com.pflegedoku.core.service.DiktatVerarbeitungsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/visiten")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class VisitenController {

    private final DiktatVerarbeitungsService diktatVerarbeitungsService;
    private final AuthSessionService authSessionService;

    public VisitenController(DiktatVerarbeitungsService diktatVerarbeitungsService, AuthSessionService authSessionService) {
        this.diktatVerarbeitungsService = diktatVerarbeitungsService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/analysieren")
    public ResponseEntity<?> analysiereDiktat(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody AnalyseRequestDto request) {

    	System.out.println("VisitenController: Teste Token '" + bearerToken + "'" );
        // 1. Auth-Header validieren
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Fehlender oder ungültiger Authorization-Header");
        }
        
        

        // 2. Token extrahieren und Session über Service abfragen
        String token = bearerToken.substring(7);
        System.out.println("VisitenController: Extrahierter Token: '" + token + "'");
        var sessionUserOpt = authSessionService.getMitarbeiterFuerToken(token);

        if (sessionUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session abgelaufen oder ungültig");
        }

        // 3. Verifizierte Mitarbeiter-ID direkt aus der aktiven Session verwenden
        MitarbeiterStammdaten sessionUser = sessionUserOpt.get();
        String verifizierteMitarbeiterId = sessionUser.mitarbeiterId();

        // 4. Verarbeitungs-Service aufrufen
        VisitenDokumentation dokumentation = diktatVerarbeitungsService.verarbeiteDiktat(
            request.audioText(),
            verifizierteMitarbeiterId,
            request.bewohnerId()
        );

        return ResponseEntity.ok(dokumentation);
    }
}