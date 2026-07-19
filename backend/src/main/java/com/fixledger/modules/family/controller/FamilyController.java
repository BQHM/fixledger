package com.fixledger.modules.family.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.request.InviteFamilyMemberRequest;
import com.fixledger.modules.family.request.UpdateFamilyMemberRoleRequest;
import com.fixledger.modules.family.request.UpdateFamilyRequest;
import com.fixledger.modules.family.response.FamilyMemberResponse;
import com.fixledger.modules.family.response.FamilyResponse;
import com.fixledger.modules.family.service.FamilyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文件功能说明：家庭空间接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@RestController
@RequestMapping("/api/families")
public class FamilyController {

  private final FamilyService familyService;

  public FamilyController(FamilyService familyService) {
    this.familyService = familyService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理家庭空间查询列表接口请求。
   * </p>
   * @return 列表结果
   */
  @GetMapping
  public Result<List<FamilyResponse>> listFamilies() {
    return Result.success(familyService.listFamilies(CurrentUserContext.getUserId()));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理家庭空间创建接口请求。
   * </p>
   * @param request 请求参数
   * @return 创建后的数据
   */
  @PostMapping
  public Result<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
    return Result.success(familyService.createFamily(CurrentUserContext.getUserId(), request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理家庭空间更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PutMapping("/{familyId}")
  public Result<FamilyResponse> updateFamily(
      @PathVariable Long familyId,
      @Valid @RequestBody UpdateFamilyRequest request
  ) {
    return Result.success(
        familyService.updateFamily(CurrentUserContext.getUserId(), familyId, request)
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理家庭空间查询列表接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @return 列表结果
   */
  @GetMapping("/{familyId}/members")
  public Result<List<FamilyMemberResponse>> listMembers(@PathVariable Long familyId) {
    return Result.success(familyService.listMembers(CurrentUserContext.getUserId(), familyId));
  }

  @PostMapping("/{familyId}/members")
  public Result<FamilyMemberResponse> inviteMember(
      @PathVariable Long familyId,
      @Valid @RequestBody InviteFamilyMemberRequest request
  ) {
    return Result.success(
        familyService.inviteMember(CurrentUserContext.getUserId(), familyId, request)
    );
  }

  @PutMapping("/{familyId}/members/{memberId}/role")
  public Result<FamilyMemberResponse> updateMemberRole(
      @PathVariable Long familyId,
      @PathVariable Long memberId,
      @Valid @RequestBody UpdateFamilyMemberRoleRequest request
  ) {
    return Result.success(
        familyService.updateMemberRole(CurrentUserContext.getUserId(), familyId, memberId, request)
    );
  }

  @DeleteMapping("/{familyId}/members/{memberId}")
  public Result<Boolean> removeMember(
      @PathVariable Long familyId,
      @PathVariable Long memberId
  ) {
    familyService.removeMember(CurrentUserContext.getUserId(), familyId, memberId);
    return Result.success(true);
  }
}
