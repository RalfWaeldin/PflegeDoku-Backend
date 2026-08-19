package com.pflegedoku.adapter.out.ai;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import com.pflegedoku.core.domain.Besonderheit;
import com.pflegedoku.core.domain.DokumentationsStatus;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.domain.Vitalwerte;
import com.pflegedoku.core.port.AiAnalysisService;

@Component
public class SpringAiAnalysisAdapter implements AiAnalysisService {

    private final ChatModel chatModel;

    public SpringAiAnalysisAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public VisitenDokumentation analysiereText(String audioText, String mitarbeiterId, String explicitBewohnerId) {
        
        var outputConverter = new BeanOutputConverter<>(ExtrahierterInhalt.class);

        String promptMessage = """
                Du bist ein medizinischer Fachassistent in einem Seniorenwohnheim.
                Deine Aufgabe ist es, den Diktattext einer Pflegekraft präzise zu analysieren, die Originalsprache zu erkennen und ALLE Informationen vollständig ins Deutsche zu übersetzen.

                Diktattext:
                "{text}"

                STRIKTE REGELN ZUR SPRACHE & ÜBERSETZUNG (ABSOLUTE PRIORITÄT):
                - JEDER einzene Textwert / String in dem generierten JSON (inklusive aller Unterobjekte, Listen, Beschreibungen, Gründe, Anmerkungen, Lokalisationen und Einnahmestatus-Gründe) MUSS AUSNAHMSLOS AUF DEUTSCH VERFASST SEIN.
                - NIEMALS Wörter oder Phrasen aus der Originalsprache (z. B. Twi, Spanisch, Englisch) in Attributwerte wie 'grundAbweichung', 'anmerkung' oder 'beschreibung' übernehmen!
                - Übersetze JEDEN Abweichungsgrund, jede Anmerkung und jede Beobachtung präzise ins Deutsche.

                STRIKTE REGELN ZUR EXTRAKTION:
                1. 'erkannteSprache': Zweistelliger ISO 639-1 Sprachcode der TATSÄCHLICHEN gesprochenen Hauptsprache des Diktattextes (z. B. "de", "en", "ak", "es", "fr", "it", "tr", "pl").
                   - Bestimme die Sprache anhand der Grammatik und des Satzbaus im Fließtext.
                   - Ignoriere deutsche Eigennamen oder medizinische Einheiten bei der Spracherkennung.

                2. 'bewohnerId': Extrahiere Nummern, die sich direkt auf den Bewohner beziehen (z. B. "B-1024" -> "B-1024"). Falls nicht genannt: null.

                3. 'bewohnerName': Der genannte Name des Bewohners (z. B. "Karl-Heinz Schmidt"). Falls nicht genannt: null.

                4. 'zimmerNummer': Reine Zimmernummer (z. B. "112"). Nur extrahieren, wenn ein Raumbegriff genannt wurde.

                5. 'vitalwerte': Extrahiere genannte Werte als Zahlen. Nicht genannte Werte MÜSSEN null sein.

                6. 'medikamente': Extrahiere ALLE im Text genannten Medikamente:
                   - 'medikamentName': Reine Bezeichnung des Medikaments (z. B. "Furosemide", "ASA").
                   - 'dosierung': Dosis/Wirkstärke falls genannt (z. B. "20 mg"), sonst null.
                   - 'status': Einnahmestatus aus ["GEGEBEN", "VERWEIGERT", "AUSGEFALLEN"].
                   - 'grundAbweichung': Grund bei Abweichung/Verweigerung ZWINGEND AUF DEUTSCH übersetzt (z. B. "leichte Verwirrtheit", "Übelkeit"), sonst null. KEINE FREMDSPRACHIGEN BEGRIFFE!
                   - 'bedarfsmedikation': boolean (true/false).

                7. 'massnahmen': Liste aller durchgeführten pflegerischen Maßnahmen:
                   - 'massnahmeCode': Kurzer Code ("KOMPR", "WUND", "MOBIL", etc.), sonst null.
                   - 'beschreibung': Kurze DEUTSCHE Bezeichnung der Maßnahme.
                   - 'durchgefuehrt': boolean (true/false).
                   - 'anmerkung': Details oder Ergänzungen ZWINGEND AUF DEUTSCH (z. B. "Klasse 2, an beiden Beinen"), sonst null.

                8. 'besonderheiten': Geäußerte Beschwerden, Auffälligkeiten oder Verhaltensänderungen:
                   - 'beschreibung': Kurze DEUTSCHE Beschreibung.
                   - 'lokalisation': Körperstelle AUF DEUTSCH (z. B. "linke Hand"), sonst null.
                   - 'kategorie': Strikte Auswahl aus ["SCHMERZ", "BEFINDLICHKEIT", "VERHALTEN", "STIMMUNG", "SONSTIGES"].

                9. 'pflegeberichtDeutsch': Professioneller, sachlicher deutscher Pflegebericht im Passiv/Fachstil.
                   - Vollständige Zusammenfassung aller Punkte auf Deutsch.

                10. Gib AUSSCHLIESSLICH das gültige JSON-Objekt zurück.

                {format}
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
        promptTemplate.add("text", audioText);
        promptTemplate.add("format", outputConverter.getFormat());

        Prompt prompt = promptTemplate.create();
        var response = chatModel.call(prompt);

        String rawContent = response.getResult().getOutput().getContent();
        String jsonContent = sanitizeJson(rawContent);

        ExtrahierterInhalt analysiert = outputConverter.convert(jsonContent);

        Vitalwerte domainVitalwerte = mapVitalwerte(analysiert.vitalwerte());

        // 1. Sprache ermitteln
        String sprache = isValiderString(analysiert.erkannteSprache())
                ? analysiert.erkannteSprache().toLowerCase()
                : "unbekannt";

        // 2. IDs bereinigen (String "null" -> echtes null)
        String rawAudioId = sanitizeLlmString(analysiert.bewohnerId());
        String rawExplicitId = sanitizeLlmString(explicitBewohnerId);

        String effektiveBewohnerId = (rawExplicitId != null) ? rawExplicitId : rawAudioId;

        // 3. Name & Zimmernummer bereinigen
        String bewohnerName = sanitizeLlmName(analysiert.bewohnerName());
        String zimmerNummer = sanitizeLlmString(analysiert.zimmerNummer());

        // 4. Strikte Validierung auf echte Daten
        boolean hatBewohnerInfo = (effektiveBewohnerId != null)
                || (bewohnerName != null || zimmerNummer != null);

        String pflegebericht = analysiert.pflegeberichtDeutsch();

        if (!hatBewohnerInfo) {
            pflegebericht = "[HINWEIS: Bewohner konnte nicht identifiziert werden!] " 
                          + (pflegebericht != null ? pflegebericht : "");
        }
        
        List<Besonderheit> domainBesonderheiten = mapBesonderheiten(analysiert.besonderheiten());

        return new VisitenDokumentation(
            UUID.randomUUID().toString(),
            effektiveBewohnerId,
            bewohnerName,
            zimmerNummer,
            mitarbeiterId,
            LocalDateTime.now(),
            sprache,
            audioText,
            pflegebericht,
            domainVitalwerte,
            analysiert.medikamente() != null ? analysiert.medikamente() : Collections.emptyList(),
            analysiert.massnahmen() != null ? analysiert.massnahmen() : Collections.emptyList(),
            domainBesonderheiten,
            DokumentationsStatus.ENTWURF
        );
    }
    
    private List<Besonderheit> mapBesonderheiten(List<ExtrahierterInhalt.BesonderheitDto> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(dto -> new Besonderheit(
                        dto.beschreibung(),
                        dto.lokalisation(),
                        dto.kategorie()
                ))
                .toList();
    }
    
    private String sanitizeLlmString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.equalsIgnoreCase("null") || trimmed.equalsIgnoreCase("undefined")) {
            return null;
        }
        return trimmed;
    }

    private String sanitizeLlmName(String input) {
        String clean = sanitizeLlmString(input);
        if (clean == null) {
            return null;
        }
        String lower = clean.toLowerCase();
        if (lower.contains("patient") || lower.contains("bewohner") || lower.equals("unbekannt")) {
            return null;
        }
        return clean;
    }

    private boolean isValiderString(String input) {
        return sanitizeLlmString(input) != null;
    }
    
    private Vitalwerte mapVitalwerte(ExtrahierterInhalt.VitalwerteDto dto) {
        if (dto == null) return null;

        Vitalwerte.Blutdruck blutdruck = null;
        if (dto.blutdruck() != null && dto.blutdruck().systolisch() != null && dto.blutdruck().diastolisch() != null) {
            blutdruck = new Vitalwerte.Blutdruck(dto.blutdruck().systolisch(), dto.blutdruck().diastolisch());
        }

        return new Vitalwerte(
            blutdruck,
            dto.pulsBpm(),
            dto.blutzuckerMgDl(),
            dto.sauerstoffSaettigungProzent(),
            dto.temperaturCelsius(),
            dto.gewichtKg(),
            dto.trinkmengeMl(),
            dto.schmerzSkalaNrs()
        );
    }

    private String sanitizeJson(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }
        return cleaned;
    }
}