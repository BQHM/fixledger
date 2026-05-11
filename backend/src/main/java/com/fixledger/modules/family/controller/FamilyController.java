package com.fixledger.modules.family.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.request.UpdateFamilyRequest;
import com.fixledger.modules.family.response.FamilyMemberResponse;
import com.fixledger.modules.family.response.FamilyResponse;
import com.fixledger.modules.family.service.FamilyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/families")
public class FamilyController {

  private final FamilyService familyService;

  public FamilyController(FamilyService familyService) {
    this.familyService = familyService;
  }

  @GetMapping
  public Result<List<FamilyResponse>> listFamilies() {
    return Result.success(familyService.listFamilies(CurrentUserContext.getUserId()));
  }

  @PostMapping
  public Result<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
    return Result.success(familyService.createFamily(CurrentUserContext.getUserId(), request));
  }

  @PutMapping("/{familyId}")
  public Result<FamilyResponse> updateFamily(
      @PathVariable Long familyId,
      @Valid @RequestBody UpdateFamilyRequest request
  ) {
    return Result.success(
        familyService.updateFamily(CurrentUserContext.getUserId(), familyId, request)
    );
  }

  @GetMapping("/{familyId}/members")
  public Result<List<FamilyMemberResponse>> listMembers(@PathVariable Long familyId) {
    return Result.success(familyService.listMembers(CurrentUserContext.getUserId(), familyId));
  }
}
