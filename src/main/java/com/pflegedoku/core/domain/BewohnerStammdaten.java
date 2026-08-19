package com.pflegedoku.core.domain;

public record BewohnerStammdaten(
    String bewohnerId,      // z.B. "B-1024"
    String vorname,         // z.B. "Karl-Heinz"
    String nachname,        // z.B. "Schmidt"
    String zimmerNummer     // z.B. "112"
) {
    public String vollstName() {
        return (vorname() + " " + nachname()).trim();
    }
}