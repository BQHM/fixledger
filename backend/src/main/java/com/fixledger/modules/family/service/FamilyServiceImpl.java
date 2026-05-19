package com.fixledger.modules.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.entity.FamilySpaceEntity;
import com.fixledger.modules.family.enums.FamilyMemberRole;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.family.mapper.FamilySpaceMapper;
import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.request.UpdateFamilyRequest;
import com.fixledger.modules.family.response.FamilyMemberResponse;
import com.fixledger.modules.family.response.FamilyResponse;
import com.fixledger.modules.user.entity.UserEntity;
import com.fixledger.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：家庭空间服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class FamilyServiceImpl implements FamilyService {

  private final FamilySpaceMapper familySpaceMapper;
  private final FamilyMemberMapper familyMemberMapper;
  private final UserMapper userMapper;

  public FamilyServiceImpl(
      FamilySpaceMapper familySpaceMapper,
      FamilyMemberMapper familyMemberMapper,
      UserMapper userMapper
  ) {
    this.familySpaceMapper = familySpaceMapper;
    this.familyMemberMapper = familyMemberMapper;
    this.userMapper = userMapper;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public FamilyResponse createFamily(Long userId, CreateFamilyRequest request) {
    return createFamilyInternal(userId, request);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param nicknameOrUsername nicknameOrUsername 参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public FamilyResponse createDefaultFamily(Long userId, String nicknameOrUsername) {
    String displayName = StringUtils.hasText(nicknameOrUsername) ? nicknameOrUsername : "我的";
    CreateFamilyRequest request = new CreateFamilyRequest(displayName + "的家", "注册时自动创建");
    return createFamilyInternal(userId, request);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间查询列表业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @return 列表结果
   */
  @Override
  public List<FamilyResponse> listFamilies(Long userId) {
    List<FamilyMemberEntity> members = familyMemberMapper.selectList(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getUserId, userId)
            .orderByAsc(FamilyMemberEntity::getId)
    );
    Map<Long, FamilySpaceEntity> families = listFamiliesById(members);
    return members.stream()
        .map(member -> {
          FamilySpaceEntity family = families.get(member.getFamilyId());
          if (family == null) {
            throw new BusinessException(ErrorCode.FAMILY_SPACE_NOT_FOUND, "家庭空间不存在");
          }
          return toFamilyResponse(family, member.getRole());
        })
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间更新业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @Override
  @Transactional
  public FamilyResponse updateFamily(Long userId, Long familyId, UpdateFamilyRequest request) {
    FamilyMemberEntity member = getMember(userId, familyId);
    if (!FamilyMemberRole.OWNER.getCode().equals(member.getRole())) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "只有家庭空间所有者可以修改家庭信息");
    }

    FamilySpaceEntity family = familySpaceMapper.selectById(familyId);
    if (family == null) {
      throw new BusinessException(ErrorCode.FAMILY_SPACE_NOT_FOUND, "家庭空间不存在");
    }
    family.setName(request.name());
    family.setDescription(request.description());
    familySpaceMapper.updateById(family);
    return toFamilyResponse(family, member.getRole());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间查询列表业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 列表结果
   */
  @Override
  public List<FamilyMemberResponse> listMembers(Long userId, Long familyId) {
    checkFamilyMember(userId, familyId);
    List<FamilyMemberEntity> members = familyMemberMapper.selectList(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getFamilyId, familyId)
            .orderByAsc(FamilyMemberEntity::getJoinedAt)
    );
    Map<Long, UserEntity> users = listUsersById(members);
    return members.stream()
        .map(member -> toMemberResponse(member, users))
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间执行业务处理业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   */
  @Override
  public void checkFamilyMember(Long userId, Long familyId) {
    getMember(userId, familyId);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现家庭空间查询业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @return 查询结果
   */
  @Override
  public Long getDefaultFamilyId(Long userId) {
    // 默认家庭取用户最早加入的家庭空间，用于登录后初始化上下文。
    FamilyMemberEntity member = familyMemberMapper.selectOne(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getUserId, userId)
            .orderByAsc(FamilyMemberEntity::getId)
            .last("LIMIT 1")
    );
    return member == null ? null : member.getFamilyId();
  }

  private FamilyResponse createFamilyInternal(Long userId, CreateFamilyRequest request) {
    // 家庭空间和成员关系一起创建，保证 owner 权限立即可用。
    FamilySpaceEntity family = new FamilySpaceEntity();
    family.setName(request.name());
    family.setDescription(request.description());
    family.setOwnerUserId(userId);
    familySpaceMapper.insert(family);

    createMember(family.getId(), userId, FamilyMemberRole.OWNER);
    return toFamilyResponse(family, FamilyMemberRole.OWNER.getCode());
  }

  private void createMember(Long familyId, Long userId, FamilyMemberRole role) {
    FamilyMemberEntity member = new FamilyMemberEntity();
    member.setFamilyId(familyId);
    member.setUserId(userId);
    member.setRole(role.getCode());
    member.setJoinedAt(LocalDateTime.now());
    familyMemberMapper.insert(member);
  }

  private FamilyMemberEntity getMember(Long userId, Long familyId) {
    // 家庭成员关系是所有 family_id 数据访问的统一隔离闸口。
    FamilyMemberEntity member = familyMemberMapper.selectOne(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getUserId, userId)
            .eq(FamilyMemberEntity::getFamilyId, familyId)
    );
    if (member == null) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该家庭空间");
    }
    return member;
  }

  private Map<Long, FamilySpaceEntity> listFamiliesById(List<FamilyMemberEntity> members) {
    Set<Long> familyIds = members.stream()
        .map(FamilyMemberEntity::getFamilyId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (familyIds.isEmpty()) {
      return Map.of();
    }
    return familySpaceMapper.selectList(new LambdaQueryWrapper<FamilySpaceEntity>()
            .in(FamilySpaceEntity::getId, familyIds))
        .stream()
        .collect(Collectors.toMap(
            FamilySpaceEntity::getId,
            family -> family,
            (left, right) -> left
        ));
  }

  private Map<Long, UserEntity> listUsersById(List<FamilyMemberEntity> members) {
    Set<Long> userIds = members.stream()
        .map(FamilyMemberEntity::getUserId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (userIds.isEmpty()) {
      return Map.of();
    }
    return userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
            .in(UserEntity::getId, userIds))
        .stream()
        .collect(Collectors.toMap(
            UserEntity::getId,
            user -> user,
            (left, right) -> left
        ));
  }

  private FamilyResponse toFamilyResponse(FamilySpaceEntity family, String role) {
    return new FamilyResponse(
        family.getId(),
        family.getName(),
        family.getDescription(),
        role,
        family.getOwnerUserId()
    );
  }

  private FamilyMemberResponse toMemberResponse(
      FamilyMemberEntity member,
      Map<Long, UserEntity> users
  ) {
    UserEntity user = users.get(member.getUserId());
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND, "家庭成员用户不存在");
    }
    return new FamilyMemberResponse(
        member.getId(),
        user.getId(),
        user.getUsername(),
        user.getNickname(),
        member.getRole(),
        member.getJoinedAt()
    );
  }
}
