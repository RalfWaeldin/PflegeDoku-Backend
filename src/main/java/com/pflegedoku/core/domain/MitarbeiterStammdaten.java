package com.pflegedoku.core.domain;

public record MitarbeiterStammdaten(
    String mitarbeiterId,
    String vorname,
    String nachname,
    String rolle,      // z. B. "Pflegefachkraft", "PDL"
    String passwort    // In Produktion gehasht, hier im Demo-Memory als Klartext/Krypt
) {}