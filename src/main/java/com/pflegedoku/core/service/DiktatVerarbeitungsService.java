package com.pflegedoku.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pflegedoku.core.domain.BewohnerStammdaten;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.port.AiAnalysisService;
import com.pflegedoku.core.port.BewohnerRepository;

@Service
public class DiktatVerarbeitungsService {

    private final AiAnalysisService aiAnalysisService;
    private final BewohnerRepository bewohnerRepository;

    public DiktatVerarbeitungsService(AiAnalysisService aiAnalysisService, BewohnerRepository bewohnerRepository) {
        this.aiAnalysisService = aiAnalysisService;
        this.bewohnerRepository = bewohnerRepository;
    }

    public VisitenDokumentation verarbeiteDiktat(String audioText, String pflegerId, String gewaehlteBewohnerId) {
        
        // 1. Reine LLM-Analyse durchführen (Outbound Port Aufruf)
        VisitenDokumentation rohErgebnis = aiAnalysisService.analysiereText(audioText, pflegerId, gewaehlteBewohnerId);

        // 2. Abgleich mit der Bewohner-Stammdatenbank
        Optional<BewohnerStammdaten> bewohnerOpt = Optional.empty();

        if (isValideId(gewaehlteBewohnerId)) {
            // Priorität 1: Explizit im Frontend ausgewählt
            bewohnerOpt = bewohnerRepository.findeMitId(gewaehlteBewohnerId);
        } else {
            // Priorität 2: Aus LLM-Extraktion in der Datenbank matchen
            bewohnerOpt = bewohnerRepository.sucheBestenTreffer(
                rohErgebnis.bewohnerId(), 
                rohErgebnis.bewohnerName(), 
                rohErgebnis.zimmerNummer()
            );
        }

        // 3. Ergebnis mit verifizierten Stammdaten anreichern
        if (bewohnerOpt.isPresent()) {
            BewohnerStammdaten stammdaten = bewohnerOpt.get();
            return rohErgebnis.mitVerifiziertenDaten(
                stammdaten.bewohnerId(),
                stammdaten.vollstName(),
                stammdaten.zimmerNummer()
            );
        }

        return rohErgebnis;
    }

    private boolean isValideId(String id) {
        return id != null && !id.isBlank() && !"UNBEKANNT".equalsIgnoreCase(id.trim());
    }
}