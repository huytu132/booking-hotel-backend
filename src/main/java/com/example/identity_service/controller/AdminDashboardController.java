//package com.example.identity_service.controller;
//
//import com.example.identity_service.dto.response.DashboardResponse;
//import com.example.identity_service.service.AdminDashboardService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/admin/dashboard")
//@RequiredArgsConstructor
//public class AdminDashboardController {
//
//    private final AdminDashboardService adminDashboardService;
//
//    @GetMapping
//    public ResponseEntity<DashboardResponse> getDashboardData() {
//        return ResponseEntity.ok(adminDashboardService.getDashboardData());
//    }
//}