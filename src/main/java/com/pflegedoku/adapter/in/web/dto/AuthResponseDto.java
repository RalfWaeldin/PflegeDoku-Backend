package com.pflegedoku.adapter.in.web.dto;

public record AuthResponseDto(
    String token, 
    String mitarbeiterId, 
    String vorname, 
    String nachname, 
    String rolle
) {}