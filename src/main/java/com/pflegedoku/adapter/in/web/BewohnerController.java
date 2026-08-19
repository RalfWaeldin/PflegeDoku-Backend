package com.pflegedoku.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pflegedoku.adapter.in.web.dto.BewohnerDropdownItemDto;
import com.pflegedoku.core.port.BewohnerRepository;

@RestController
@RequestMapping("/api/bewohner")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class BewohnerController {

    private final BewohnerRepository bewohnerRepository;

    public BewohnerController(BewohnerRepository bewohnerRepository) {
        this.bewohnerRepository = bewohnerRepository;
    }

    @GetMapping
    public List<BewohnerDropdownItemDto> getAlleBewohner() {
        return bewohnerRepository.findeAlle().stream()
                .map(BewohnerDropdownItemDto::ausDomain)
                .toList();
    }
}