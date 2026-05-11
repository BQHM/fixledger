package com.fixledger.modules.dashboard.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import com.fixledger.modules.dashboard.response.DeviceCategoryDistributionResponse;
import com.fixledger.modules.dashboard.response.MaintenanceCostTrendResponse;
import com.fixledger.modules.dashboard.response.ReminderCalendarDayResponse;
import com.fixledger.modules.dashboard.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/families/{familyId}/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/summary")
  public Result<DashboardSummaryResponse> summary(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(dashboardService.summary(userId, familyId));
  }

  @GetMapping("/device-category-distribution")
  public Result<List<DeviceCategoryDistributionResponse>> deviceCategoryDistribution(
      @PathVariable Long familyId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(dashboardService.deviceCategoryDistribution(userId, familyId));
  }

  @GetMapping("/maintenance-cost-trend")
  public Result<List<MaintenanceCostTrendResponse>> maintenanceCostTrend(
      @PathVariable Long familyId,
      @RequestParam(required = false) Integer months
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(dashboardService.maintenanceCostTrend(userId, familyId, months));
  }

  @GetMapping("/reminder-calendar")
  public Result<List<ReminderCalendarDayResponse>> reminderCalendar(
      @PathVariable Long familyId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        dashboardService.reminderCalendar(userId, familyId, startDate, endDate)
    );
  }
}
