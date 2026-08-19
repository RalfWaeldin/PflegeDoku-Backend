package com.pflegedoku.adapter.out.targetsystem;

import org.springframework.stereotype.Component;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.port.TargetSystemExporter;

@Component
public class MyNevaSwingAdapter implements TargetSystemExporter {

    @Override
    public String getSystemId() {
        return "MYNEVA_SWING";
    }

    @Override
    public ExportResult export(VisitenDokumentation doku) {
        return new ExportResult(true, "Vorbereitet für spätere Swing-Übernahme");
    }
}