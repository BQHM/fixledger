package com.fixledger.modules.family.service;

import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.request.UpdateFamilyRequest;
import com.fixledger.modules.family.response.FamilyMemberResponse;
import com.fixledger.modules.family.response.FamilyResponse;
import java.util.List;

/**
 * 家庭空间服务，是所有家庭业务数据隔离的入口。
 */
public interface FamilyService {

  /**
   * 为用户创建一个新的家庭空间，并将用户设为所有者。
   *
   * @param userId 当前用户 ID
   * @param request 家庭空间创建请求
   * @return 家庭空间信息
   */
  FamilyResponse createFamily(Long userId, CreateFamilyRequest request);

  /**
   * 注册后创建默认家庭空间，保证用户首次登录即可录入设备。
   *
   * @param userId 当前用户 ID
   * @param nicknameOrUsername 昵称或用户名
   * @return 默认家庭空间信息
   */
  FamilyResponse createDefaultFamily(Long userId, String nicknameOrUsername);

  /**
   * 查询用户已加入的家庭空间列表。
   *
   * @param userId 当前用户 ID
   * @return 家庭空间列表
   */
  List<FamilyResponse> listFamilies(Long userId);

  /**
   * 更新家庭空间资料，当前只允许所有者操作。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 更新请求
   * @return 更新后的家庭空间信息
   */
  FamilyResponse updateFamily(Long userId, Long familyId, UpdateFamilyRequest request);

  /**
   * 查询家庭成员列表，访问前会校验当前用户是否属于该家庭。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 家庭成员列表
   */
  List<FamilyMemberResponse> listMembers(Long userId, Long familyId);

  /**
   * 校验用户是否属于指定家庭空间。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   */
  void checkFamilyMember(Long userId, Long familyId);

  /**
   * 获取用户默认家庭空间，用于登录后初始化前端上下文。
   *
   * @param userId 当前用户 ID
   * @return 默认家庭空间 ID，若不存在则返回 null
   */
  Long getDefaultFamilyId(Long userId);
}
