package com.pflegedoku.core.domain;

public record PflegeMassnahme(
    String massnahmeCode,
    String beschreibung,
    boolean durchgefuehrt,
    String anmerkung
) {}