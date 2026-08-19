package com.pflegedoku.core.domain;

public record MedikamentenEreignis(
    String medikamentName,
    String dosierung,
    EinnahmeStatus status,
    String grundAbweichung,
    boolean bedarfsmedikation
) {}