package com.pflegedoku.adapter.out.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.pflegedoku.core.domain.DokumentationsStatus;
import com.pflegedoku.core.domain.MedikamentenEreignis;
import com.pflegedoku.core.domain.VisitenDokumentation;

@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.root=INFO",
        "logging.level.org.springframework.ai=WARN",
        "logging.level.org.springframework.web=WARN",
        "logging.level.org.springframework.web.client.RestTemplate=WARN",
        "logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions=WARN",
        "logging.level.com.pflegedoku=INFO"
})
class SpringAiAnalysisAdapterTest {

    @Autowired
    private SpringAiAnalysisAdapter aiAnalysisAdapter;
    
    private void  printTestHeader(String title, String audioText, String pflegerId, String bewohnerId) {
    	System.out.println("===========================================================");
    	System.out.println(title);
    	System.out.println("-----------------------------------------------------------");
        System.out.println("BODY AUDIO TEXT:");
        System.out.println(audioText);
        System.out.println("BODY PFLEGER ID: " + pflegerId);
        System.out.println("BODY BEWOHNER ID: " + bewohnerId);
    }
    
    private void printErgebnis(VisitenDokumentation ergebnis) {
    	System.out.println("--------------------------------------------------------------");
        System.out.println("ERGEBNIS STATUS: " + ergebnis.status());
        System.out.println("ERGEBNIS SPRACHE: " + ergebnis.originalSprache());
        System.out.println("ERGEBNIS BEWOHNER ID: " + ergebnis.bewohnerId());
        System.out.println("ERGEBNIS BEWOHNER NAME: " + ergebnis.bewohnerName());
        System.out.println("ERGEBNIS BEWOHNER ZIMMER NUMMER: " + ergebnis.zimmerNummer());
        if (ergebnis.vitalwerte() != null) {
            if (ergebnis.vitalwerte().blutdruck() != null) {
                System.out.println("ERGEBNIS SYSTOLISCH: " + ergebnis.vitalwerte().blutdruck().systolisch());
                System.out.println("ERGEBNIS DIASTOLISCH: " + ergebnis.vitalwerte().blutdruck().diastolisch());
            } else {
                System.out.println("ERGEBNIS BLUTDRUCK: null");
            }
            System.out.println("ERGEBNIS PULS: " + ergebnis.vitalwerte().pulsBpm());
        } else {
            System.out.println("ERGEBNIS VITALWERTE: null");
        }
        System.out.println("--------------------------------------------------------------");
    } 

    @Test
    void testAnalysiereSpanischesDiktatMitBewohnerIdImBody() {
    	
    	
    	String requestAudioText = """
                La Sra. Müller tenía una presión arterial de 130 sobre 85 y un pulso de 75. 
                Tomó su medicación de la mañana sin problemas, pero rechazó el almuerzo por náuseas.
                """;
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "bewohner-123";
        
        printTestHeader("TEST - ANALYSIERE SPANISCHES DIKTAT MIT BEWOHNER ID IM BODY", requestAudioText, requestPflegerId, requestBewohnerId);

        // Aufruf mit der neuen Signatur (audioText, mitarbeiterId, explicitBewohnerId)
        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
        	requestAudioText, 
        	requestPflegerId,
            requestBewohnerId
        );
       
        assertNotNull(ergebnis);
        
        printErgebnis(ergebnis);
        
        assertEquals(DokumentationsStatus.ENTWURF, ergebnis.status());
        
        assertEquals("bewohner-123", ergebnis.bewohnerId());
        assertEquals("es", ergebnis.originalSprache());
        
        assertNotNull(ergebnis.pflegeberichtDeutsch());
        
        // Vitalwerte prüfen
        assertNotNull(ergebnis.vitalwerte());
        assertEquals(130, ergebnis.vitalwerte().blutdruck().systolisch());
        assertEquals(85, ergebnis.vitalwerte().blutdruck().diastolisch());
        assertEquals(75, ergebnis.vitalwerte().pulsBpm());
        
        System.out.println("=== DEUTSCHER PFLEGEBERICHT ===");
        System.out.println(ergebnis.pflegeberichtDeutsch());
        
     
        
        
    }
    
    @Test
    void testAnalysiereSpanischesDiktatOhneBewohnerIdImBody() {
    	String requestAudioText = """
                La señora Huanita Müller, de la habitación 47, tenía una presión arterial de 130/85 y un pulso de 75. Tomó su medicación matutina sin problemas, pero rechazó el almuerzo debido a náuseas.
                """;
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "";
        
        printTestHeader("TEST - ANALYSIERE SPANISCHES DIKTAT OHNE BEWOHNER ID IM BODY", requestAudioText, requestPflegerId, requestBewohnerId);

        // Aufruf mit der neuen Signatur (audioText, mitarbeiterId, explicitBewohnerId)
        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
        	requestAudioText, 
        	requestPflegerId,
            ""
        );
        
        assertNotNull(ergebnis);
        
        printErgebnis(ergebnis);
        
        assertEquals(DokumentationsStatus.ENTWURF, ergebnis.status());
        assertEquals("es", ergebnis.originalSprache());
        assertNotNull(ergebnis.bewohnerName());
        assertTrue(ergebnis.bewohnerName().contains("Huanita Müller"));
        assertNotNull(ergebnis.zimmerNummer());
        assertEquals("47", ergebnis.zimmerNummer());
        
        // Vitalwerte prüfen
        assertNotNull(ergebnis.vitalwerte());
        
        assertEquals(130, ergebnis.vitalwerte().blutdruck().systolisch());
        assertEquals(85, ergebnis.vitalwerte().blutdruck().diastolisch());
        assertEquals(75, ergebnis.vitalwerte().pulsBpm());
        
        assertNotNull(ergebnis.pflegeberichtDeutsch());
        System.out.println("=== DEUTSCHER PFLEGEBERICHT ===");
        System.out.println(ergebnis.pflegeberichtDeutsch());
        
        
        
    }
    
    @Test
    void testAnalysiereSpanischesDiktatmitBewohnerIdImAudioText() {
    	String requestAudioText = """
                La residente ciento veintitrés tenía una presión arterial de 130/85 y un pulso de 75. Tomó su medicación matutina sin problemas, pero rechazó el almuerzo debido a náuseas.
                """;
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "";
        
        printTestHeader("TEST - ANALYSIERE SPANISCHES DIKTAT MIT BEWOHNER ID AUDIO TEXT", requestAudioText, requestPflegerId, requestBewohnerId);

        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
        	requestAudioText, 
        	requestPflegerId,
            ""
        );
       
        assertNotNull(ergebnis);
        
        printErgebnis(ergebnis);
        
        assertEquals(DokumentationsStatus.ENTWURF, ergebnis.status());
        
        assertNotNull(ergebnis.bewohnerId());
        assertTrue(ergebnis.bewohnerId().contains("123"));
        assertEquals("es", ergebnis.originalSprache());
        
        // Vitalwerte prüfen
        assertNotNull(ergebnis.vitalwerte(), "Vitalwerte sollten nicht null sein");
        assertNotNull(ergebnis.vitalwerte().blutdruck(), "Blutdruck sollte vom LLM extrahiert werden");
        assertEquals(130, ergebnis.vitalwerte().blutdruck().systolisch());
        assertEquals(85, ergebnis.vitalwerte().blutdruck().diastolisch());
        
        assertNotNull(ergebnis.vitalwerte().pulsBpm(), "Puls sollte vom LLM extrahiert werden");
        assertEquals(75, ergebnis.vitalwerte().pulsBpm());
        
        assertNotNull(ergebnis.pflegeberichtDeutsch());
        System.out.println("=== DEUTSCHER PFLEGEBERICHT ===");
        System.out.println(ergebnis.pflegeberichtDeutsch());
        
     
    }

    @Test
    void testAnalysiereDiktatOhneJeglicheBewohnerIdentifikation() {
    	String requestAudioText = """
                Der Blutdruck lag bei 120/80, Puls 70. Patient hat gut gefrühstückt.
                """;
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "";
        
        printTestHeader("TEST - ANALYSIERE DIKTAT OHNE JEGLICHE BEWOHNER IDENTIFIKATION", requestAudioText, requestPflegerId, requestBewohnerId);

        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
        	requestAudioText, 
        	requestPflegerId,
            ""
        );
        
        assertNotNull(ergebnis);
        
        printErgebnis(ergebnis);
        
        assertEquals("de", ergebnis.originalSprache());
        
        // Vitalwerte prüfen
        assertNotNull(ergebnis.vitalwerte(), "Vitalwerte sollten nicht null sein");
        assertNotNull(ergebnis.vitalwerte().blutdruck(), "Blutdruck sollte vom LLM extrahiert werden");
        assertEquals(120, ergebnis.vitalwerte().blutdruck().systolisch());
        assertEquals(80, ergebnis.vitalwerte().blutdruck().diastolisch());
        assertNotNull(ergebnis.vitalwerte().pulsBpm(), "Puls sollte vom LLM extrahiert werden");
        assertEquals(70, ergebnis.vitalwerte().pulsBpm());
        
        System.out.println("=== PFLEGEBERICHT DEUTSCH (preview) ===");
        System.out.println(ergebnis.pflegeberichtDeutsch());
        
        assertTrue(ergebnis.pflegeberichtDeutsch().contains("Bewohner konnte nicht identifiziert werden!"));
        System.out.println("=== PFLEGEBERICHT DEUTSCH ===");
        System.out.println(ergebnis.pflegeberichtDeutsch());
    }

    @Test
    void testAnalysiereDiktatMitAbgelehntemMedikament() {
        String requestAudioText = """
                Frau Marianne Weber in Zimmer 12 hat heute Morgen die Einnahme von Ramipril 5mg abgelehnt wegen Übelkeit. ASS 100 hat sie wie gewohnt eingenommen.
                """;
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "";

        printTestHeader("TEST - ANALYSIERE DIKTAT MIT ABGELEHNTEM MEDIKAMENT", requestAudioText, requestPflegerId, requestBewohnerId);

        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
            requestAudioText,
            requestPflegerId,
            requestBewohnerId
        );

        assertNotNull(ergebnis);
        printErgebnis(ergebnis);

        // Basis-Asserterungen
        assertEquals(DokumentationsStatus.ENTWURF, ergebnis.status());
        assertEquals("12", ergebnis.zimmerNummer());
        assertEquals("Marianne Weber", ergebnis.bewohnerName());

        // Medikamente prüfen
        List<MedikamentenEreignis> medikamente = ergebnis.medikamente();
        assertNotNull(medikamente, "Medikamentenliste sollte nicht null sein");
        assertFalse(medikamente.isEmpty(), "Es sollten Medikamente aus dem Diktat extrahiert werden");

        // Prüfen, ob Ramipril extrahiert wurde
        var ramiprilEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("ramipril"))
                .findFirst();

        assertTrue(ramiprilEreignis.isPresent(), "Ramipril sollte in der Medikamentenliste enthalten sein");

        
        // Optional: Eigenschaften von Ramipril genauer prüfen (je nach Feldern deines MedikamentenEreignis Domain-Objekts)
        ramiprilEreignis.ifPresent(med -> {
            System.out.println("=== GEFUNDENES ABGELEHNTES MEDIKAMENT ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });
        
     // ASS prüfen (Eingenommen)
        var assEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("ass"))
                .findFirst();
        assertTrue(assEreignis.isPresent(), "ASS 100 sollte als eingenommenes Medikament extrahiert werden");
        
        assEreignis.ifPresent(med -> {
            System.out.println("=== GEFUNDENES ABGELEHNTES MEDIKAMENT ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });

        // Prüfen, ob der deutsche Pflegebericht den Hinweis auf die Verweigerung enthält
        assertNotNull(ergebnis.pflegeberichtDeutsch());
        assertTrue(ergebnis.pflegeberichtDeutsch().toLowerCase().contains("ramipril"), "Pflegebericht sollte Ramipril erwähnen");
    }
    
    @Test
    void testDiktatKomplettDeutsch() {
        String requestAudioText = """
                Dokumentation für Bewohner ID B-1024, Herr Karl-Heinz Schmidt aus Zimmer 112. 
Morgenvisite und Vitalwerte: Der Blutdruck liegt bei 138 zu 84 mmHg, Puls 74 Schläge pro Minute, Blutzucker nüchtern 125 mg/dL. Die Sauerstoffsättigung beträgt 96 Prozent, Körpertemperatur 36,8 Grad Celsius. Das Gewicht beträgt heute Morgen 78,5 kg und die Trinkmenge liegt bisher bei 1200 ml. Auf der Schmerzskala gibt der Bewohner aktuell eine 3 von 10 an.
Medikation: Amlodipin 5 mg sowie ASS 100 mg wurden regulär verabreicht. Furosemid 20 mg wurde heute Morgen aufgrund von leichtem Schwindel abgelehnt. Wegen der Knieschmerzen wurden zusätzlich 20 Tropfen Novalgin als Bedarfsmedikation gegeben.
Durchgeführte Maßnahmen: Kompressionsstrümpfe der Klasse 2 an beiden Beinen angelegt. Wundversorgung an der rechten Ferse durchgeführt und Verband erneuert. Mobilisation mit dem Rollator auf dem Flur für 15 Minuten begleitet. Brief an Familie.
Besonderheiten: Der Bewohner wirkte heute Morgen etwas unruhig und sorgte sich bezüglich des angekündigten Familienbesuchs am Nachmittag. An beiden Unterschenkeln zeigen sich leichte Ödemneigungen. Der Bewohner klagte über Schmerzen im linken Handgelenk. 
""";
        String requestPflegerId = "pfleger-456";
        String requestBewohnerId = "";

        printTestHeader("TEST - ANALYSIERE DIKTAT KOMPLETT DEUTSCH", requestAudioText, requestPflegerId, requestBewohnerId);

        VisitenDokumentation ergebnis = aiAnalysisAdapter.analysiereText(
            requestAudioText,
            requestPflegerId,
            requestBewohnerId
        );

        assertNotNull(ergebnis);
        printErgebnis(ergebnis);

        // Basis-Asserterungen
        assertEquals(DokumentationsStatus.ENTWURF, ergebnis.status());
        assertEquals("112", ergebnis.zimmerNummer());
        assertEquals("Karl-Heinz Schmidt", ergebnis.bewohnerName());

        // Medikamente prüfen
        List<MedikamentenEreignis> medikamente = ergebnis.medikamente();
        assertNotNull(medikamente, "Medikamentenliste sollte nicht null sein");
        assertFalse(medikamente.isEmpty(), "Es sollten Medikamente aus dem Diktat extrahiert werden");

        // Prüfen, ob Amlodipin extrahiert wurde
        var amlodipinEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("amlodipin"))
                .findFirst();

        assertTrue(amlodipinEreignis.isPresent(), "Amlodipin sollte in der Medikamentenliste enthalten sein");
        
        // Optional: Eigenschaften von Amlodipin genauer prüfen (je nach Feldern deines MedikamentenEreignis Domain-Objekts)
        amlodipinEreignis.ifPresent(med -> {
            System.out.println("=== Amlodipin DETAILS ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });
        
        // Prüfen, ob ASS extrahiert wurde
        var assEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("ass"))
                .findFirst();

        assertTrue(assEreignis.isPresent(), "ASS sollte in der Medikamentenliste enthalten sein");
        
        // Optional: Eigenschaften von ASS genauer prüfen (je nach Feldern deines MedikamentenEreignis Domain-Objekts)
        assEreignis.ifPresent(med -> {
            System.out.println("=== ASS DETAILS ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });
        
        // Prüfen, ob Furosemid extrahiert wurde
        var furosemidEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("furosemid"))
                .findFirst();

        assertTrue(furosemidEreignis.isPresent(), "Furosemid sollte in der Medikamentenliste enthalten sein");
        
        // Optional: Eigenschaften von furosemidEreignis genauer prüfen (je nach Feldern deines MedikamentenEreignis Domain-Objekts)
        furosemidEreignis.ifPresent(med -> {
            System.out.println("=== Furosemid DETAILS ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });

        // Prüfen, ob Novalgin extrahiert wurde
        var novalginEreignis = medikamente.stream()
                .filter(m -> m.medikamentName() != null && m.medikamentName().toLowerCase().contains("novalgin"))
                .findFirst();

        assertTrue(novalginEreignis.isPresent(), "Novalgin sollte in der Medikamentenliste enthalten sein");
        
        // Optional: Eigenschaften von novalginEreignis genauer prüfen (je nach Feldern deines MedikamentenEreignis Domain-Objekts)
        novalginEreignis.ifPresent(med -> {
            System.out.println("=== Novalgin DETAILS ===");
            System.out.println("Name: " + med.medikamentName());
            System.out.println("Dosis: " + med.dosierung());
            System.out.println("Status: " + med.status()); // z.B. "ABGELEHNT" / "VERWEIGERT"
            System.out.println("Grund: " + med.grundAbweichung()); // z.B. "Übelkeit"
        });
      

        // Prüfen, ob der deutsche Pflegebericht den Hinweis auf die Verweigerung enthält
        assertNotNull(ergebnis.pflegeberichtDeutsch());
        //assertTrue(ergebnis.pflegeberichtDeutsch().toLowerCase().contains("ramipril"), "Pflegebericht sollte Ramipril erwähnen");
    }
}