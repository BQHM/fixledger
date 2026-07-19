package com.fixledger.modules.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.request.InviteFamilyMemberRequest;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.system.query.OperationLogPageQuery;
import com.fixledger.modules.system.response.OperationLogResponse;
import com.fixledger.modules.system.service.OperationLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OperationLogServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private OperationLogService operationLogService;

  @Test
  @DisplayName("家庭协作操作会写入当前家庭可查询的操作日志")
  void familyMemberOperationCreatesQueryableLog() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "logowner",
        null,
        "123456",
        "Log Owner"
    ));
    authService.register(new RegisterRequest(
        "logmember",
        null,
        "123456",
        "Log Member"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());
    familyService.inviteMember(
        owner.userId(),
        familyId,
        new InviteFamilyMemberRequest("logmember", "MEMBER")
    );
    OperationLogPageQuery query = new OperationLogPageQuery();
    query.setFamilyId(familyId);
    query.setModule("FAMILY");

    PageResponse<OperationLogResponse> logs = operationLogService.pageLogs(owner.userId(), query);

    assertThat(logs.records()).extracting(OperationLogResponse::action)
        .contains("INVITE_MEMBER");
    assertThat(logs.records()).allSatisfy(log -> assertThat(log.familyId()).isEqualTo(familyId));
  }
}
