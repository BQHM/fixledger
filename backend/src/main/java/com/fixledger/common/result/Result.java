package com.fixledger.common.result;

import com.fixledger.common.exception.ErrorCode;

/**
 * <p>
 * 文件功能说明：统一响应组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
public record Result<T>(Integer code, String message, T data) {

  private static final int SUCCESS_CODE = 0;
  private static final String SUCCESS_MESSAGE = "success";
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：构造成功响应。
   * </p>
   * @return 统一响应结果
   */
  public static <T> Result<T> success() {
    return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：构造成功响应。
   * </p>
   * @param data 响应数据
   * @return 统一响应结果
   */
  public static <T> Result<T> success(T data) {
    return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：构造失败响应。
   * </p>
   * @param errorCode 错误码
   * @return 统一响应结果
   */
  public static <T> Result<T> error(ErrorCode errorCode) {
    return error(errorCode.getCode(), errorCode.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：构造失败响应。
   * </p>
   * @param errorCode 错误码
   * @param message 错误信息
   * @return 统一响应结果
   */
  public static <T> Result<T> error(ErrorCode errorCode, String message) {
    return error(errorCode.getCode(), message);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：构造失败响应。
   * </p>
   * @param code 编码值
   * @param message 错误信息
   * @return 统一响应结果
   */
  public static <T> Result<T> error(Integer code, String message) {
    return new Result<>(code, message, null);
  }
}
