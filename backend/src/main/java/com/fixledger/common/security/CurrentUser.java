package com.fixledger.common.security;

/**
 * <p>
 * 文件功能说明：认证安全组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
public record CurrentUser(Long id, String username) {
}
