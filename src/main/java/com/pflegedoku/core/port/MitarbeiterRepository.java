package com.pflegedoku.core.port;

import org.springframework.stereotype.Repository;

import com.pflegedoku.core.domain.MitarbeiterStammdaten;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MitarbeiterRepository {

    private final Map<String, MitarbeiterStammdaten> store = new ConcurrentHashMap<>();

    public MitarbeiterRepository() {
        // Simulationsdaten für myneva Swing Mock
        store.put("pfleger-456", new MitarbeiterStammdaten("pfleger-456", "Maria", "Müller", "Pflegefachkraft", "geheim123"));
        store.put("pdl-001", new MitarbeiterStammdaten("pdl-001", "Thomas", "Weber", "PDL", "admin123"));
    }

    public Optional<MitarbeiterStammdaten> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}