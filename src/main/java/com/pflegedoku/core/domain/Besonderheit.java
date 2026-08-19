package com.pflegedoku.core.domain;

public record Besonderheit(
    String beschreibung,
    String lokalisation,
    String kategorie
) {}