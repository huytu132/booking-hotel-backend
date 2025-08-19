package com.example.identity_service.controller;

import com.example.identity_service.dto.request.AddonRequest;
import com.example.identity_service.dto.response.AddonResponse;
import com.example.identity_service.service.AddonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    @GetMapping
    public List<AddonResponse> getAll() {
        return addonService.getAll();
    }

    @GetMapping("/{id}")
    public AddonResponse getById(@PathVariable int id) {
        return addonService.getById(id);
    }

    @PostMapping
    public AddonResponse create(@RequestBody AddonRequest request) {
        return addonService.create(request);
    }

    @PutMapping("/{id}")
    public AddonResponse update(@PathVariable int id, @RequestBody AddonRequest request) {
        return addonService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        addonService.delete(id);
    }
}
