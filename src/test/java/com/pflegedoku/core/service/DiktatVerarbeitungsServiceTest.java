package com.pflegedoku.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pflegedoku.core.domain.Besonderheit;
import com.pflegedoku.core.domain.BewohnerStammdaten;
import com.pflegedoku.core.domain.DokumentationsStatus;
import com.pflegedoku.core.domain.MedikamentenEreignis;
import com.pflegedoku.core.domain.PflegeMassnahme;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.domain.Vitalwerte;
import com.pflegedoku.core.port.AiAnalysisService;
import com.pflegedoku.core.port.BewohnerRepository;

@ExtendWith(MockitoExtension.class)
class DiktatVerarbeitungsServiceTest {

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private BewohnerRepository bewohnerRepository;

    @InjectMocks
    private DiktatVerarbeitungsService diktatVerarbeitungsService;

    @Test
    @DisplayName("Sollte Bewohner direkt über explizit gewählte BewohnerID matchen")
    void verarbeiteDiktat_MitExpliziterBewohnerId() {
        // GIVEN
        String audioText = "Ramipril abgelehnt wegen Übelkeit.";
        String pflegerId = "pfleger-456";
        String explicitBewohnerId = "B-1025";

        VisitenDokumentation rohErgebnis = erzeugeDummyDokumentation(null, null, null);
        BewohnerStammdaten stammdaten = new BewohnerStammdaten("B-1025", "Marianne", "Weber", "12");

        when(aiAnalysisService.analysiereText(audioText, pflegerId, explicitBewohnerId)).thenReturn(rohErgebnis);
        when(bewohnerRepository.findeMitId("B-1025")).thenReturn(Optional.of(stammdaten));

        // WHEN
        VisitenDokumentation ergebnis = diktatVerarbeitungsService.verarbeiteDiktat(audioText, pflegerId, explicitBewohnerId);

        // THEN
        assertNotNull(ergebnis);
        assertEquals("B-1025", ergebnis.bewohnerId());
        assertEquals("Marianne Weber", ergebnis.bewohnerName());
        assertEquals("12", ergebnis.zimmerNummer());

        // Verifizieren, dass der direkte ID-Lookup genutzt wurde und KEIN Matching über den Text stattfand
        verify(bewohnerRepository, times(1)).findeMitId("B-1025");
        verify(bewohnerRepository, never()).sucheBestenTreffer(any(), any(), any());
    }

    @Test
    @DisplayName("Sollte Bewohner über LLM-extrahierte Daten matchen, wenn keine Frontend-ID gewählt wurde")
    void verarbeiteDiktat_OhneFrontendId_MitLLMMatch() {
        // GIVEN
        String audioText = "Herr Schmidt aus Zimmer 112 hatte Blutdruck 138 zu 84.";
        String pflegerId = "pfleger-456";
        String explicitBewohnerId = "UNBEKANNT";

        VisitenDokumentation rohErgebnis = erzeugeDummyDokumentation(null, "Herr Karl-Heinz Schmidt", "112");
        BewohnerStammdaten stammdaten = new BewohnerStammdaten("B-1024", "Karl-Heinz", "Schmidt", "112");

        when(aiAnalysisService.analysiereText(audioText, pflegerId, explicitBewohnerId)).thenReturn(rohErgebnis);
        when(bewohnerRepository.sucheBestenTreffer(null, "Herr Karl-Heinz Schmidt", "112"))
                .thenReturn(Optional.of(stammdaten));

        // WHEN
        VisitenDokumentation ergebnis = diktatVerarbeitungsService.verarbeiteDiktat(audioText, pflegerId, explicitBewohnerId);

        // THEN
        assertNotNull(ergebnis);
        assertEquals("B-1024", ergebnis.bewohnerId());
        assertEquals("Karl-Heinz Schmidt", ergebnis.bewohnerName());
        assertEquals("112", ergebnis.zimmerNummer());

        // Hinweistext im Pflegebericht sollte durch die Verifizierung automatisch entfernt worden sein
        assertTrue(!ergebnis.pflegeberichtDeutsch().contains("[HINWEIS: Bewohner konnte nicht identifiziert werden!]"));

        verify(bewohnerRepository, never()).findeMitId(anyString());
        verify(bewohnerRepository, times(1)).sucheBestenTreffer(null, "Herr Karl-Heinz Schmidt", "112");
    }

    @Test
    @DisplayName("Sollte unidentifiziert bleiben, wenn weder Frontend-ID noch LLM-Extraktion in der DB matchen")
    void verarbeiteDiktat_UnbekannterBewohner() {
        // GIVEN
        String audioText = "Der Blutdruck lag bei 120/80, Patient hat gut gefrühstückt.";
        String pflegerId = "pfleger-456";
        String explicitBewohnerId = null;

        VisitenDokumentation rohErgebnis = erzeugeDummyDokumentation(null, null, null);

        when(aiAnalysisService.analysiereText(audioText, pflegerId, explicitBewohnerId)).thenReturn(rohErgebnis);
        when(bewohnerRepository.sucheBestenTreffer(null, null, null)).thenReturn(Optional.empty());

        // WHEN
        VisitenDokumentation ergebnis = diktatVerarbeitungsService.verarbeiteDiktat(audioText, pflegerId, explicitBewohnerId);

        // THEN
        assertNotNull(ergebnis);
        assertNull(ergebnis.bewohnerId());
        assertNull(ergebnis.bewohnerName());
        assertNull(ergebnis.zimmerNummer());
        
        // Hinweistext muss im Bericht erhalten bleiben
        assertTrue(ergebnis.pflegeberichtDeutsch().contains("[HINWEIS: Bewohner konnte nicht identifiziert werden!]"));
    }

    // Helper zur Erzeugung von Doku-Objekten
 // Helper zur Erzeugung von Doku-Objekten
    private VisitenDokumentation erzeugeDummyDokumentation(String id, String name, String zimmer) {
        String bericht = (id == null && name == null && zimmer == null)
                ? "[HINWEIS: Bewohner konnte nicht identifiziert werden!] Der Blutdruck lag bei 120/80."
                : "Der Blutdruck lag bei 120/80.";

        return new VisitenDokumentation(
                "doku-101",
                id,
                name,
                zimmer,
                "pfleger-456",
                LocalDateTime.now(),
                "de",
                "Audio-Text",
                bericht,
                null, // Vitalwerte (oder ein Vitalwerte-Dummy-Objekt)
                List.of(), // List<MedikamentenEreignis>
                List.of(), // List<PflegeMassnahme>
                List.of(), // List<Besonderheit>
                DokumentationsStatus.ENTWURF
        );
    }
    
}