package com.pflegedoku.core.service;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.pflegedoku.core.domain.MitarbeiterStammdaten;


@Service
public class AuthSessionService {

    private final Map<String, MitarbeiterStammdaten> activeSessions = new ConcurrentHashMap<>();

    // Erwarte den generierten JWT-Token als Parameter
    public void erstelleSession(String jwtToken, MitarbeiterStammdaten mitarbeiter) {
    	System.out.println("erstelleSession - Token: '" + jwtToken + "'");
        activeSessions.put(jwtToken, mitarbeiter);
    }

    public Optional<MitarbeiterStammdaten> getMitarbeiterFuerToken(String token) {
    	System.out.println("getMitarbeiterFuerToken - Token: '" + token + "'");
        return Optional.ofNullable(activeSessions.get(token));
    }

    public void entferneSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}