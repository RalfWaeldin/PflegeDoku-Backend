package com.pflegedoku.core.port;

import com.pflegedoku.adapter.out.targetsystem.ExportResult;
import com.pflegedoku.core.domain.VisitenDokumentation;

public interface TargetSystemExporter {
    String getSystemId();
    ExportResult export(VisitenDokumentation doku);
}