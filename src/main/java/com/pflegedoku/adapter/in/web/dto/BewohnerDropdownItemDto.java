package com.pflegedoku.adapter.in.web.dto;

import com.pflegedoku.core.domain.BewohnerStammdaten;

public record BewohnerDropdownItemDto(
    String bewohnerId,
    String vorname,
    String nachname,
    String zimmerNummer,
    String anzeigeName
) {
    public static BewohnerDropdownItemDto ausDomain(BewohnerStammdaten b) {
        String formatiertesAnzeigeName = String.format("%s - %s, %s (Zimmer %s)",
                b.bewohnerId(), 
                b.nachname(), 
                b.vorname(), 
                b.zimmerNummer());

        return new BewohnerDropdownItemDto(
            b.bewohnerId(),
            b.vorname(),
            b.nachname(),
            b.zimmerNummer(),
            formatiertesAnzeigeName
        );
    }
}