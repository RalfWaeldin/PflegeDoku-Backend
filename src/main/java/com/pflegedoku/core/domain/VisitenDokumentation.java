package com.pflegedoku.core.domain;

import java.time.LocalDateTime;
import java.util.List;

public record VisitenDokumentation(
    String id,
    String bewohnerId,
    String bewohnerName,    // NEU: Z.B. "Huanita Müller"
    String zimmerNummer,    // NEU: Z.B. "47"
    String mitarbeiterId,
    LocalDateTime erfassungsZeitpunkt,
    String originalSprache,
    String originalAudioText,
    String pflegeberichtDeutsch,
    Vitalwerte vitalwerte,
    List<MedikamentenEreignis> medikamente,
    List<PflegeMassnahme> massnahmen,
    List<Besonderheit> besonderheiten,
    DokumentationsStatus status
) {
	
	public VisitenDokumentation mitVerifiziertenDaten(String neueBewohnerId, String neuerName, String neueZimmerNummer) {
	    // Falls der Hinweis "Bewohner konnte nicht identifiziert werden!" im Pflegebericht steht, entfernen wir ihn jetzt:
	    String bereinigterBericht = this.pflegeberichtDeutsch();
	    if (bereinigterBericht != null) {
	        bereinigterBericht = bereinigterBericht.replace("[HINWEIS: Bewohner konnte nicht identifiziert werden!] ", "").trim();
	    }

	    return new VisitenDokumentation(
	        this.id(),
	        neueBewohnerId,
	        neuerName,
	        neueZimmerNummer,
	        this.mitarbeiterId(),
	        this.erfassungsZeitpunkt(),
	        this.originalSprache(),
	        this.originalAudioText(),
	        bereinigterBericht,
	        this.vitalwerte(),
	        this.medikamente(),
	        this.massnahmen(),
	        this.besonderheiten(),
	        this.status()
	    );
	}
};





