package com.fixledger.modules.reminder.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.reminder.query.ReminderPageQuery;
import com.fixledger.modules.reminder.response.ReminderResponse;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.response.UnreadCountResponse;
import com.fixledger.modules.reminder.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文件功能说明：提醒通知接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@RestController
@RequestMapping("/api/families/{familyId}/reminders")
public class ReminderController {

  private final ReminderService reminderService;

  public ReminderController(ReminderService reminderService) {
    this.reminderService = reminderService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理提醒分页查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @GetMapping
  public Result<PageResponse<ReminderResponse>> pageReminders(
      @PathVariable Long familyId,
      @Valid ReminderPageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.pageReminders(userId, familyId, query));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理提醒执行业务处理接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @return 统一响应结果
   */
  @GetMapping("/unread-count")
  public Result<UnreadCountResponse> unreadCount(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.unreadCount(userId, familyId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理提醒标记提醒接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 更新后的数据
   */
  @PatchMapping("/{reminderId}/read")
  public Result<ReminderResponse> markRead(
      @PathVariable Long familyId,
      @PathVariable Long reminderId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.markRead(userId, familyId, reminderId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理提醒忽略提醒接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 更新后的数据
   */
  @PatchMapping("/{reminderId}/ignore")
  public Result<ReminderResponse> ignore(
      @PathVariable Long familyId,
      @PathVariable Long reminderId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.ignore(userId, familyId, reminderId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理提醒扫描提醒接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @return 统一响应结果
   */
  @PostMapping("/scan")
  public Result<ReminderScanResponse> scan(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(reminderService.scanFamily(userId, familyId));
  }
}
