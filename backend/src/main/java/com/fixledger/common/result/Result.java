package com.fixledger.common.result;

import com.fixledger.common.exception.ErrorCode;

public record Result<T>(Integer code, String message, T data) {

  private static final int SUCCESS_CODE = 0;
  private static final String SUCCESS_MESSAGE = "success";

  public static <T> Result<T> success() {
    return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
  }

  public static <T> Result<T> success(T data) {
    return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
  }

  public static <T> Result<T> error(ErrorCode errorCode) {
    return error(errorCode.getCode(), errorCode.getMessage());
  }

  public static <T> Result<T> error(ErrorCode errorCode, String message) {
    return error(errorCode.getCode(), message);
  }

  public static <T> Result<T> error(Integer code, String message) {
    return new Result<>(code, message, null);
  }
}
