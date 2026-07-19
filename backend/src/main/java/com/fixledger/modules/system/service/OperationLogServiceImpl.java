package com.fixledger.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.system.entity.OperationLogEntity;
import com.fixledger.modules.system.mapper.OperationLogMapper;
import com.fixledger.modules.system.query.OperationLogPageQuery;
import com.fixledger.modules.system.response.OperationLogResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogServiceImpl implements OperationLogService {

  private static final int ERROR_LIMIT = 1024;

  private final OperationLogMapper operationLogMapper;
  private final FamilyMemberMapper familyMemberMapper;

  public OperationLogServiceImpl(
      OperationLogMapper operationLogMapper,
      FamilyMemberMapper familyMemberMapper
  ) {
    this.operationLogMapper = operationLogMapper;
    this.familyMemberMapper = familyMemberMapper;
  }

  @Override
  public void recordSuccess(
      Long userId,
      Long familyId,
      String module,
      String action,
      String bizType,
      Long bizId,
      String requestMethod,
      String requestUri
  ) {
    OperationLogEntity log = new OperationLogEntity();
    log.setUserId(userId);
    log.setFamilyId(familyId);
    log.setModule(module);
    log.setAction(action);
    log.setBizType(bizType);
    log.setBizId(bizId);
    log.setRequestMethod(requestMethod);
    log.setRequestUri(requestUri);
    log.setSuccess(true);
    log.setCreatedAt(LocalDateTime.now());
    operationLogMapper.insert(log);
  }

  @Override
  public PageResponse<OperationLogResponse> pageLogs(Long userId, OperationLogPageQuery query) {
    List<Long> familyIds = visibleFamilyIds(userId, query.getFamilyId());
    if (familyIds.isEmpty()) {
      return new PageResponse<>(
          query.getPageNum(),
          query.getPageSize(),
          0,
          0,
          List.of()
      );
    }

    LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<OperationLogEntity>()
        .in(OperationLogEntity::getFamilyId, familyIds)
        .eq(
            StringUtils.hasText(query.getModule()),
            OperationLogEntity::getModule,
            query.getModule()
        )
        .eq(
            StringUtils.hasText(query.getAction()),
            OperationLogEntity::getAction,
            query.getAction()
        )
        .orderByDesc(OperationLogEntity::getCreatedAt)
        .orderByDesc(OperationLogEntity::getId);
    Page<OperationLogEntity> page = operationLogMapper.selectPage(query.toPage(), wrapper);
    Page<OperationLogResponse> responsePage = Page.of(
        page.getCurrent(),
        page.getSize(),
        page.getTotal()
    );
    responsePage.setRecords(page.getRecords().stream().map(this::toResponse).toList());
    return PageResponse.from(responsePage);
  }

  private List<Long> visibleFamilyIds(Long userId, Long familyId) {
    List<FamilyMemberEntity> members = familyMemberMapper.selectList(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getUserId, userId)
            .eq(familyId != null, FamilyMemberEntity::getFamilyId, familyId)
    );
    if (familyId != null && members.isEmpty()) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该家庭空间操作日志");
    }
    return members.stream()
        .map(FamilyMemberEntity::getFamilyId)
        .filter(id -> id != null)
        .distinct()
        .toList();
  }

  private OperationLogResponse toResponse(OperationLogEntity entity) {
    return new OperationLogResponse(
        entity.getId(),
        entity.getUserId(),
        entity.getFamilyId(),
        entity.getModule(),
        entity.getAction(),
        entity.getBizType(),
        entity.getBizId(),
        entity.getRequestMethod(),
        entity.getRequestUri(),
        entity.getIpAddress(),
        entity.getSuccess(),
        limit(entity.getErrorMessage(), ERROR_LIMIT),
        entity.getCreatedAt()
    );
  }

  private String limit(String value, int maxLength) {
    if (!StringUtils.hasText(value) || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }
}
