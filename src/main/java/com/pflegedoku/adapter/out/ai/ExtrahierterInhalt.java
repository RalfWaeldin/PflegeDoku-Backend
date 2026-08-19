package com.pflegedoku.adapter.out.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.pflegedoku.core.domain.MedikamentenEreignis;
import com.pflegedoku.core.domain.PflegeMassnahme;

public record ExtrahierterInhalt(
    String erkannteSprache,      // Zweistelliger ISO-Code (z.B. "de", "it", "pl")
    @JsonPropertyDescription("Eindeutige System-ID des Bewohners (z. B. B-1234). MUSS null sein, wenn im Diktat nur ein Name oder eine Zimmernummer genannt wird!")
    String bewohnerId,           // Vom Diktat extrahierte ID (falls eingesprochen)
    @JsonPropertyDescription("Der vollständige Name des Bewohners aus dem Diktat (z. B. Herr Johann Schneider).")
    String bewohnerName,         // Z.B. "Frau Müller" oder "Herr Rossi"
    @JsonPropertyDescription("Reine Zimmer- oder Raumnummer als String (z. B. '17' oder '102b'). Nicht mit bewohnerId verwechseln!")
    String zimmerNummer,         // Z.B. "102" oder "Zimmer 12"
    String pflegeberichtDeutsch, // Pflichtfeld: Vollständiger deutscher Pflegebericht
    VitalwerteDto vitalwerte,
    List<MedikamentenEreignis> medikamente,
    List<PflegeMassnahme> massnahmen,
    List<BesonderheitDto> besonderheiten
) {
	public record BesonderheitDto(
	        String beschreibung,   // z.B. "Leichte Knieschmerzen"
	        String lokalisation,   // z.B. "Knie rechts"
	        String kategorie       // z.B. "SCHMERZ", "BEFINDLICHKEIT", "VERHALTEN", "SONSTIGES"
	    ) {}
	
    public record VitalwerteDto(
        BlutdruckDto blutdruck,
        Integer pulsBpm,
        Double blutzuckerMgDl,
        Double sauerstoffSaettigungProzent,
        Double temperaturCelsius,
        Double gewichtKg,
        Integer trinkmengeMl,
        Integer schmerzSkalaNrs
    ) {}

    public record BlutdruckDto(
        Integer systolisch,
        Integer diastolisch
    ) {}
}