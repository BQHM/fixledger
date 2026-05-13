package com.fixledger.common.exception;

/**
 * <p>
 * 文件功能说明：统一异常类，封装业务异常和错误码信息。
 * </p>
 *
 * @Author FixLedger
 */
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成查询异常处理。
   * </p>
   * @return 查询结果
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
