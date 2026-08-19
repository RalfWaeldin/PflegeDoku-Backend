package com.pflegedoku.core.domain;

public record Vitalwerte(
    Blutdruck blutdruck,
    Integer pulsBpm,
    Double blutzuckerMgDl,
    Double sauerstoffSaettigungProzent,
    Double temperaturCelsius,
    Double gewichtKg,
    Integer trinkmengeMl,
    Integer schmerzSkalaNrs // 0 bis 10
) {
    public record Blutdruck(
        int systolisch,
        int diastolisch
    ) {}
}