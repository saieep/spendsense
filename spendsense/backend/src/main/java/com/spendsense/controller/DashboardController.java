package com.spendsense.controller;

import com.spendsense.dto.DashboardSummaryResponse;
import com.spendsense.security.SecurityUtils;
import com.spendsense.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    public DashboardController(DashboardService dashboardService, SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return dashboardService.getSummary(securityUtils.getCurrentUserId(), from, to);
    }
}
