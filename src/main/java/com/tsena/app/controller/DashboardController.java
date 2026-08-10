package com.tsena.app.controller;

import com.tsena.app.dto.DashboardGlobalDTO;
import com.tsena.app.dto.DashboardSiteDTO;
import com.tsena.app.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or @siteAccessService.aAcces(#siteId, authentication)")
    public DashboardSiteDTO dashboardSite(@PathVariable Long siteId,
                                           @RequestParam(defaultValue = "jour") String periode) {
        return dashboardService.dashboardParSite(siteId, periode);
    }

    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardGlobalDTO dashboardGlobal(@RequestParam(defaultValue = "jour") String periode) {
        return dashboardService.dashboardGlobal(periode);
    }
}
