package com.pflegedoku.adapter.out.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.pflegedoku.core.domain.BewohnerStammdaten;

class InMemoryBewohnerAdapterTest {

    private InMemoryBewohnerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryBewohnerAdapter();
    }

    @Nested
    @DisplayName("Tier 1 & ID: Eindeutige und kombinierte Treffer")
    class EindeutigeTrefferTests {

        @Test
        @DisplayName("Priorität 1: Direkter ID-Match hat Vorrang")
        void findeMitId_Erfolgreich() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer("B-1025", "Falscher Name", "999");

            assertTrue(ergebnis.isPresent());
            assertEquals("B-1025", ergebnis.get().bewohnerId());
            assertEquals("Marianne", ergebnis.get().vorname());
        }

        @Test
        @DisplayName("Tier 1: Name UND Zimmernummer stimmen überein")
        void sucheBestenTreffer_NameUndZimmerPerfekt() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "Schmidt", "112");

            assertTrue(ergebnis.isPresent());
            assertEquals("B-1024", ergebnis.get().bewohnerId());
            assertEquals("Karl-Heinz", ergebnis.get().vorname());
        }

        @Test
        @DisplayName("Tier 1: Anreden (Herr, Frau, Señora) werden sauber normalisiert")
        void sucheBestenTreffer_NameMitAnreden() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "La señora Huanita Müller", "47");

            assertTrue(ergebnis.isPresent());
            assertEquals("B-1026", ergebnis.get().bewohnerId());
            assertEquals("Huanita", ergebnis.get().vorname());
        }
    }

    @Nested
    @DisplayName("Tier 2 & 3: Match nur über Name oder nur über Zimmer")
    class TeiltrefferTests {

        @Test
        @DisplayName("Tier 2: Nur Name angegeben, Zimmer fehlt -> Match über Vor-/Nachname")
        void sucheBestenTreffer_NurNameOhneZimmer() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "Frau Weber", null);

            assertTrue(ergebnis.isPresent());
            assertEquals("B-1025", ergebnis.get().bewohnerId());
            assertEquals("Weber", ergebnis.get().nachname());
        }

        @Test
        @DisplayName("Tier 3: Nur Zimmer angegeben, Name fehlt -> Match über Zimmernummer")
        void sucheBestenTreffer_NurZimmerOhneName() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, null, "17");

            assertTrue(ergebnis.isPresent());
            assertEquals("B-1027", ergebnis.get().bewohnerId());
            assertEquals("Johann", ergebnis.get().vorname());
        }
    }

    @Nested
    @DisplayName("Konflikte & Fehlschläge")
    class KonfliktTests {

        @Test
        @DisplayName("Konflikt 1: Name passt zu Person A, aber Zimmer gehört zu Person B -> Kein Match")
        void sucheBestenTreffer_NameUndZimmerWidersprechenSich() {
            // Schmidt wohnt in 112, in Zimmer 12 wohnt Marianne Weber
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "Herr Schmidt", "12");

            assertFalse(ergebnis.isPresent(), "Bei Widerspruch zwischen Name und Zimmer darf kein Treffer zurückgeliefert werden!");
        }

        @Test
        @DisplayName("Konflikt 2: Zimmer existiert, aber genannter Name passt nicht dazu -> Kein Match")
        void sucheBestenTreffer_ZimmerMatchMitFalschemNamen() {
            // In Zimmer 47 wohnt Huanita Müller, nicht Schneider
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "Frau Schneider", "47");

            assertFalse(ergebnis.isPresent());
        }

        @Test
        @DisplayName("Kein Match: Weder Name noch Zimmer in Stammdaten vorhanden")
        void sucheBestenTreffer_UnbekannteDaten() {
            Optional<BewohnerStammdaten> ergebnis = adapter.sucheBestenTreffer(null, "Unbekannter Name", "999");

            assertFalse(ergebnis.isPresent());
        }

        @Test
        @DisplayName("Kein Match: Alle Parameter leer oder null")
        void sucheBestenTreffer_LeereEingaben() {
            Optional<BewohnerStammdaten> ergebnis1 = adapter.sucheBestenTreffer(null, "", "   ");
            Optional<BewohnerStammdaten> ergebnis2 = adapter.sucheBestenTreffer(null, null, null);

            assertFalse(ergebnis1.isPresent());
            assertFalse(ergebnis2.isPresent());
        }
    }
}