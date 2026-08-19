package com.pflegedoku.core.port;

import com.pflegedoku.core.domain.BewohnerStammdaten;
import java.util.List;
import java.util.Optional;

public interface BewohnerRepository {
    List<BewohnerStammdaten> findeAlle();
    Optional<BewohnerStammdaten> findeMitId(String bewohnerId);
    Optional<BewohnerStammdaten> sucheBestenTreffer(String idOpt, String nameOpt, String zimmerOpt);
}