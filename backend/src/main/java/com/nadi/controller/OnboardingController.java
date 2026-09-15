package com.nadi.controller;

import com.nadi.dto.OnboardingRequest;
import com.nadi.dto.OnboardingResponse;
import com.nadi.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    public ResponseEntity<OnboardingResponse> onboard(@Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(onboardingService.onboard(request));
    }
}
