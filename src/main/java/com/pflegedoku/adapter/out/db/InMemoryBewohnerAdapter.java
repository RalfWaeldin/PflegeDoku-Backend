package com.pflegedoku.adapter.out.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.pflegedoku.core.domain.BewohnerStammdaten;
import com.pflegedoku.core.port.BewohnerRepository;

@Repository
public class InMemoryBewohnerAdapter implements BewohnerRepository {

    private final List<BewohnerStammdaten> bewohnerListe = new ArrayList<>();

    public InMemoryBewohnerAdapter() {
        bewohnerListe.add(new BewohnerStammdaten("B-1024", "Karl-Heinz", "Schmidt", "112"));
        bewohnerListe.add(new BewohnerStammdaten("B-1025", "Marianne", "Weber", "12"));
        bewohnerListe.add(new BewohnerStammdaten("B-1026", "Huanita", "Müller", "47"));
        bewohnerListe.add(new BewohnerStammdaten("B-1027", "Johann", "Schneider", "17"));
    }

    @Override
    public List<BewohnerStammdaten> findeAlle() {
        return List.copyOf(bewohnerListe);
    }

    @Override
    public Optional<BewohnerStammdaten> findeMitId(String bewohnerId) {
        if (bewohnerId == null || bewohnerId.isBlank()) {
            return Optional.empty();
        }
        return bewohnerListe.stream()
                .filter(b -> b.bewohnerId().equalsIgnoreCase(bewohnerId.trim()))
                .findFirst();
    }

    @Override
    public Optional<BewohnerStammdaten> sucheBestenTreffer(String idOpt, String nameOpt, String zimmerOpt) {
        // 1. Höchste Priorität: Direkter Treffer über die ID
        if (idOpt != null && !idOpt.isBlank()) {
            Optional<BewohnerStammdaten> byId = findeMitId(idOpt);
            if (byId.isPresent()) {
                return byId;
            }
        }

        boolean hatName = nameOpt != null && !nameOpt.isBlank();
        boolean hatZimmer = zimmerOpt != null && !zimmerOpt.isBlank();

        if (!hatName && !hatZimmer) {
            return Optional.empty();
        }

        String normalisierterName = hatName ? normalisiereText(nameOpt) : "";

        // TIER 1: Name UND Zimmer stimmen überein (Idealfall)
        if (hatName && hatZimmer) {
            Optional<BewohnerStammdaten> kombiMatch = bewohnerListe.stream()
                    .filter(b -> matchZimmer(b, zimmerOpt) && matchName(b, normalisierterName))
                    .findFirst();
            if (kombiMatch.isPresent()) {
                return kombiMatch;
            }
        }

        // TIER 2: Name stimmt überein (z. B. "Frau Marianne Weber" ohne Zimmerangabe)
        if (hatName) {
            List<BewohnerStammdaten> nameMatches = bewohnerListe.stream()
                    .filter(b -> matchName(b, normalisierterName))
                    .toList();

            if (nameMatches.size() == 1) {
                BewohnerStammdaten kandidat = nameMatches.get(0);
                // Sicherheit: Falls eine Zimmernummer angegeben war, muss sie zum Kandidaten passen (kein Widerspruch)
                if (!hatZimmer || matchZimmer(kandidat, zimmerOpt)) {
                    return Optional.of(kandidat);
                }
            }
        }

        // TIER 3: Nur Zimmer stimmt überein – ABER NUR wenn im Diktat kein Name genannt wurde
        if (hatZimmer && !hatName) {
            List<BewohnerStammdaten> zimmerMatches = bewohnerListe.stream()
                    .filter(b -> matchZimmer(b, zimmerOpt))
                    .toList();

            if (zimmerMatches.size() == 1) {
                return Optional.of(zimmerMatches.get(0));
            }
        }

        // Kein eindeutiger oder widerspruchsfreier Treffer
        return Optional.empty();
    }

    private boolean matchZimmer(BewohnerStammdaten b, String zimmerOpt) {
        if (zimmerOpt == null || zimmerOpt.isBlank()) {
            return false;
        }
        return b.zimmerNummer().trim().equalsIgnoreCase(zimmerOpt.trim());
    }

    private boolean matchName(BewohnerStammdaten b, String normalisierterInputName) {
        String nachname = b.nachname().toLowerCase();
        String vorname = b.vorname().toLowerCase();

        // Treffer, wenn Nachname oder Vorname im normalisierten Eingabetext vorkommt
        return normalisierterInputName.contains(nachname) || normalisierterInputName.contains(vorname);
    }

    private String normalisiereText(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replace("herr", "")
                .replace("frau", "")
                .replace(" señora", "")
                .replace(" señor", "")
                .replace(" patient", "")
                .replaceAll("[^a-zäöüß0-9\\s]", " ")
                .trim();
    }
}