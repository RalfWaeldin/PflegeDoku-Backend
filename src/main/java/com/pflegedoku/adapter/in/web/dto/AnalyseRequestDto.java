package com.pflegedoku.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyseRequestDto(
	@NotBlank(message = "Audio-Text darf nicht leer sein")String audioText,
    //@NotBlank(message = "Mitarbeiter-ID darf nicht leer sein") String mitarbeiterId,
    String bewohnerId
) {}