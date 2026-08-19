package com.pflegedoku.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pflegedoku.adapter.out.targetsystem.ExportResult;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.port.TargetSystemExporter;

@Service
public class ExportService {

    private final Map<String, TargetSystemExporter> exporterMap;

    public ExportService(List<TargetSystemExporter> exporters) {
        this.exporterMap = exporters.stream()
                .collect(Collectors.toMap(TargetSystemExporter::getSystemId, e -> e));
    }

    public ExportResult exportToSystem(String systemId, VisitenDokumentation doku) {
        TargetSystemExporter exporter = exporterMap.get(systemId);
        if (exporter == null) {
            throw new IllegalArgumentException("Unbekanntes Zielsystem: " + systemId);
        }
        return exporter.export(doku);
    }
}