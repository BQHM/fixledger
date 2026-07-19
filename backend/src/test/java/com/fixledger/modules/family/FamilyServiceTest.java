package com.fixledger.modules.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.request.InviteFamilyMemberRequest;
import com.fixledger.modules.family.request.UpdateFamilyMemberRoleRequest;
import com.fixledger.modules.family.request.UpdateFamilyRequest;
import com.fixledger.modules.family.response.FamilyMemberResponse;
import com.fixledger.modules.family.response.FamilyResponse;
import com.fixledger.modules.family.service.FamilyService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FamilyServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Test
  @DisplayName("用户只能查询自己所属的家庭空间")
  void listFamiliesOnlyReturnsJoinedFamilies() {
    RegisterResponse first = authService.register(new RegisterRequest(
        "usera",
        null,
        "123456",
        "A"
    ));
    RegisterResponse second = authService.register(new RegisterRequest(
        "userb",
        null,
        "123456",
        "B"
    ));
    familyService.createFamily(first.userId(), new CreateFamilyRequest("A 的第二个家", null));

    List<FamilyResponse> firstFamilies = familyService.listFamilies(first.userId());
    List<FamilyResponse> secondFamilies = familyService.listFamilies(second.userId());

    assertThat(firstFamilies).hasSize(2);
    assertThat(secondFamilies).hasSize(1);
  }

  @Test
  @DisplayName("非家庭成员访问家庭空间时抛出无权限异常")
  void checkFamilyMemberRejectsNonMember() {
    RegisterResponse first = authService.register(new RegisterRequest(
        "userc",
        null,
        "123456",
        "C"
    ));
    RegisterResponse second = authService.register(new RegisterRequest(
        "userd",
        null,
        "123456",
        "D"
    ));
    Long firstFamilyId = familyService.getDefaultFamilyId(first.userId());

    assertThatThrownBy(() -> familyService.checkFamilyMember(second.userId(), firstFamilyId))
        .isInstanceOfSatisfying(BusinessException.class, e ->
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  @Test
  @DisplayName("家庭所有者可以修改家庭空间并查询成员")
  void ownerCanUpdateFamilyAndListMembers() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "usere",
        null,
        "123456",
        "E"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());

    FamilyResponse updated = familyService.updateFamily(
        owner.userId(),
        familyId,
        new UpdateFamilyRequest("杭州的家", "杭州住处设备")
    );
    List<FamilyMemberResponse> members = familyService.listMembers(owner.userId(), familyId);

    assertThat(updated.name()).isEqualTo("杭州的家");
    assertThat(updated.description()).isEqualTo("杭州住处设备");
    assertThat(members).hasSize(1);
    assertThat(members.getFirst().role()).isEqualTo("OWNER");
  }

  @Test
  @DisplayName("家庭所有者可以邀请已注册用户加入家庭")
  void ownerCanInviteRegisteredUser() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "familyowner",
        null,
        "123456",
        "Owner"
    ));
    RegisterResponse member = authService.register(new RegisterRequest(
        "familymember",
        "member@example.com",
        "123456",
        "Member"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());

    FamilyMemberResponse invited = familyService.inviteMember(
        owner.userId(),
        familyId,
        new InviteFamilyMemberRequest("member@example.com", "MEMBER")
    );
    List<FamilyResponse> joinedFamilies = familyService.listFamilies(member.userId());

    assertThat(invited.userId()).isEqualTo(member.userId());
    assertThat(invited.role()).isEqualTo("MEMBER");
    assertThat(joinedFamilies).extracting(FamilyResponse::id).contains(familyId);
  }

  @Test
  @DisplayName("普通成员不能邀请其他用户")
  void memberCannotInviteUser() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "familyowner2",
        null,
        "123456",
        "Owner"
    ));
    RegisterResponse member = authService.register(new RegisterRequest(
        "familymember2",
        null,
        "123456",
        "Member"
    ));
    RegisterResponse other = authService.register(new RegisterRequest(
        "familyother2",
        null,
        "123456",
        "Other"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());
    familyService.inviteMember(
        owner.userId(),
        familyId,
        new InviteFamilyMemberRequest("familymember2", "MEMBER")
    );

    assertThatThrownBy(() -> familyService.inviteMember(
        member.userId(),
        familyId,
        new InviteFamilyMemberRequest(other.username(), "MEMBER")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  @Test
  @DisplayName("家庭所有者可以调整成员角色并移除成员")
  void ownerCanUpdateRoleAndRemoveMember() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "familyowner3",
        null,
        "123456",
        "Owner"
    ));
    authService.register(new RegisterRequest(
        "familymember3",
        null,
        "123456",
        "Member"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());
    FamilyMemberResponse member = familyService.inviteMember(
        owner.userId(),
        familyId,
        new InviteFamilyMemberRequest("familymember3", "MEMBER")
    );

    FamilyMemberResponse ownerRole = familyService.updateMemberRole(
        owner.userId(),
        familyId,
        member.id(),
        new UpdateFamilyMemberRoleRequest("OWNER")
    );
    familyService.removeMember(owner.userId(), familyId, member.id());
    List<FamilyMemberResponse> members = familyService.listMembers(owner.userId(), familyId);

    assertThat(ownerRole.role()).isEqualTo("OWNER");
    assertThat(members).extracting(FamilyMemberResponse::id).doesNotContain(member.id());
  }

  @Test
  @DisplayName("不能移除或降级最后一个家庭所有者")
  void cannotRemoveOrDowngradeLastOwner() {
    RegisterResponse owner = authService.register(new RegisterRequest(
        "familyowner4",
        null,
        "123456",
        "Owner"
    ));
    Long familyId = familyService.getDefaultFamilyId(owner.userId());
    FamilyMemberResponse ownerMember = familyService
        .listMembers(owner.userId(), familyId)
        .getFirst();

    assertThatThrownBy(() -> familyService.updateMemberRole(
        owner.userId(),
        familyId,
        ownerMember.id(),
        new UpdateFamilyMemberRoleRequest("MEMBER")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));

    assertThatThrownBy(() -> familyService.removeMember(
        owner.userId(),
        familyId,
        ownerMember.id()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
  }
}

