package com.fixledger.common.security;

/**
 * <p>
 * 文件功能说明：当前登录用户上下文，保存业务身份和本次 JWT 的唯一标识。
 * </p>
 *
 * @Author FixLedger
 */
public record CurrentUser(Long id, String username, String tokenId) {
}
