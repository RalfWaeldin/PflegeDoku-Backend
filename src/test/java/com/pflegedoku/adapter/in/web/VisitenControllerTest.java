package com.pflegedoku.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pflegedoku.adapter.in.web.dto.AnalyseRequestDto;
import com.pflegedoku.config.JwtService;
import com.pflegedoku.core.domain.DokumentationsStatus;
import com.pflegedoku.core.domain.MitarbeiterStammdaten;
import com.pflegedoku.core.domain.VisitenDokumentation;
import com.pflegedoku.core.service.AuthSessionService;
import com.pflegedoku.core.service.DiktatVerarbeitungsService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitenController.class)
@AutoConfigureMockMvc(addFilters = false)
class VisitenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiktatVerarbeitungsService diktatVerarbeitungsService;

    @MockBean
    private AuthSessionService authSessionService; // NEU: Mock für die Session-Verwaltung
    
    @MockBean
    private JwtService jwtService; // <-- NEU: Fehlende Dependency für JwtAuthenticationFilter mocken

    @Test
    @DisplayName("POST /api/visiten/analysieren - Erfolgreiche Analyse mit gültigem Auth-Token")
    void testAnalysiereDiktatErfolgreich() throws Exception {
        // GIVEN
        String validToken = "valid-test-token";
        MitarbeiterStammdaten mockMitarbeiter = new MitarbeiterStammdaten(
            "pfleger-123", "Maria", "Müller", "Pflegefachkraft", "pass123"
        );

        AnalyseRequestDto requestDto = new AnalyseRequestDto(
            "Herr Schmidt hatte Blutdruck 120 zu 80.",
            //"pfleger-123",
            "B-1024"
        );

        VisitenDokumentation mockDokumentation = new VisitenDokumentation(
            "doku-1",
            "B-1024",
            "Karl-Heinz Schmidt",
            "112",
            "pfleger-123",
            LocalDateTime.now(),
            "de",
            requestDto.audioText(),
            "Blutdruck lag bei 120/80.",
            null,
            List.of(),
            List.of(),
            List.of(),
            DokumentationsStatus.ENTWURF
        );

        // Session Mocking: Token liefert gültigen Mitarbeiter
        when(authSessionService.getMitarbeiterFuerToken(validToken))
                .thenReturn(Optional.of(mockMitarbeiter));

        // Diktatverarbeitung Mocking
        when(diktatVerarbeitungsService.verarbeiteDiktat(
                eq(requestDto.audioText()),
                eq("pfleger-123"), // Wird aus dem Session-Mitarbeiter genommen
                eq(requestDto.bewohnerId())
        )).thenReturn(mockDokumentation);

        // WHEN & THEN
        mockMvc.perform(post("/api/visiten/analysieren")
                .header("Authorization", "Bearer " + validToken) // Authorization-Header mitgeben
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doku-1"))
                .andExpect(jsonPath("$.bewohnerId").value("B-1024"))
                .andExpect(jsonPath("$.bewohnerName").value("Karl-Heinz Schmidt"))
                .andExpect(jsonPath("$.zimmerNummer").value("112"));

        verify(diktatVerarbeitungsService).verarbeiteDiktat(
                requestDto.audioText(),
                "pfleger-123",
                requestDto.bewohnerId()
        );
    }

    @Test
    @DisplayName("POST /api/visiten/analysieren - Ohne Authorization-Header wird HTTP 401 Unauthorized geliefert")
    void testAnalysiereDiktatOhneAuthHeader() throws Exception {
        AnalyseRequestDto requestDto = new AnalyseRequestDto(
            "Herr Schmidt hatte Blutdruck 120 zu 80.",
            //"pfleger-123",
            "B-1024"
        );

        mockMvc.perform(post("/api/visiten/analysieren")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(diktatVerarbeitungsService);
    }

    @Test
    @DisplayName("POST /api/visiten/analysieren - Fehlender audioText liefert HTTP 400 Bad Request bei gültigem Token")
    void testAnalysiereDiktatFehlenderAudioText() throws Exception {
        // GIVEN
        String validToken = "valid-test-token";
        MitarbeiterStammdaten mockMitarbeiter = new MitarbeiterStammdaten(
            "pfleger-456", "Thomas", "Weber", "PDL", "pass123"
        );

        when(authSessionService.getMitarbeiterFuerToken(validToken))
                .thenReturn(Optional.of(mockMitarbeiter));

        Map<String, String> requestBody = Map.of(
            "mitarbeiterId", "pfleger-456",
            "bewohnerId", "bewohner-202"
            // "audioText" fehlt bewusst
        );

        // WHEN & THEN
        mockMvc.perform(post("/api/visiten/analysieren")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(diktatVerarbeitungsService);
    }
}