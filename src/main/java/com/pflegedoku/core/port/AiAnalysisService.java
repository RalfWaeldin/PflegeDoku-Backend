package com.pflegedoku.core.port;

import com.pflegedoku.core.domain.VisitenDokumentation;

public interface AiAnalysisService {

    /**
     * Analysiert ein Audiodiktat, erkennt automatisch die Sprache, übersetzt den Text
     * ins Deutsche und extrahiert strukturierte Daten.
     */
    VisitenDokumentation analysiereText(String audioText, String mitarbeiterId, String explicitBewohnerId);
}