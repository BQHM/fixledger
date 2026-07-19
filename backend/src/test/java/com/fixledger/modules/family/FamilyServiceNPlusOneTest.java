package com.fixledger.modules.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.entity.FamilySpaceEntity;
import com.fixledger.modules.family.enums.FamilyMemberRole;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.family.mapper.FamilySpaceMapper;
import com.fixledger.modules.family.response.FamilyResponse;
import com.fixledger.modules.family.service.FamilyServiceImpl;
import com.fixledger.modules.system.service.OperationLogService;
import com.fixledger.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyServiceNPlusOneTest {

  @Test
  @DisplayName("家庭列表批量加载家庭空间，避免按成员逐条查询家庭")
  void listFamiliesBatchLoadsFamilySpaces() {
    FamilySpaceMapper familySpaceMapper = mock(FamilySpaceMapper.class);
    FamilyMemberMapper familyMemberMapper = mock(FamilyMemberMapper.class);
    UserMapper userMapper = mock(UserMapper.class);
    DeviceCategoryMapper deviceCategoryMapper = mock(DeviceCategoryMapper.class);
    OperationLogService operationLogService = mock(OperationLogService.class);
    FamilyServiceImpl service = new FamilyServiceImpl(
        familySpaceMapper,
        familyMemberMapper,
        userMapper,
        deviceCategoryMapper,
        operationLogService
    );
    FamilyMemberEntity first = member(1L, 10L, 100L);
    FamilyMemberEntity second = member(2L, 11L, 100L);

    when(familyMemberMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(first, second));
    when(familySpaceMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(family(10L, "杭州的家"), family(11L, "老家的家")));

    List<FamilyResponse> responses = service.listFamilies(100L);

    assertThat(responses).extracting(FamilyResponse::name)
        .containsExactly("杭州的家", "老家的家");
    verify(familySpaceMapper).selectList(any(LambdaQueryWrapper.class));
    verify(familySpaceMapper, never()).selectById(10L);
    verify(familySpaceMapper, never()).selectById(11L);
  }

  private FamilyMemberEntity member(Long id, Long familyId, Long userId) {
    FamilyMemberEntity member = new FamilyMemberEntity();
    member.setId(id);
    member.setFamilyId(familyId);
    member.setUserId(userId);
    member.setRole(FamilyMemberRole.OWNER.getCode());
    member.setJoinedAt(LocalDateTime.of(2026, 5, 15, 10, 0));
    return member;
  }

  private FamilySpaceEntity family(Long id, String name) {
    FamilySpaceEntity family = new FamilySpaceEntity();
    family.setId(id);
    family.setName(name);
    family.setOwnerUserId(100L);
    return family;
  }
}
