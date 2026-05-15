package com.fixledger.common.exception;

import com.fixledger.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * <p>
 * 文件功能说明：统一异常处理器，将业务异常、参数异常和系统异常转换为统一响应结构。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 处理业务异常，并按错误码语义映射合适的 HTTP 状态。
   *
   * @param e 业务异常
   * @return 带 HTTP 状态的统一错误响应
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
    return ResponseEntity.status(resolveHttpStatus(e.getErrorCode()))
        .body(Result.error(e.getErrorCode(), e.getMessage()));
  }

  /**
   * 处理请求体参数校验异常。
   *
   * @param e 参数校验异常
   * @return 统一错误响应
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(this::formatFieldError)
        .collect(Collectors.joining("; "));
    return Result.error(ErrorCode.BAD_REQUEST, message);
  }

  /**
   * 处理路径变量、查询参数等约束校验异常。
   *
   * @param e 约束校验异常
   * @return 统一错误响应
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
    String message = e.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
        .collect(Collectors.joining("; "));
    return Result.error(ErrorCode.BAD_REQUEST, message);
  }

  /**
   * 处理缺少参数、类型转换失败和 JSON 请求体不可读等基础请求错误。
   *
   * @param e 请求解析异常
   * @return 统一错误响应
   */
  @ExceptionHandler({
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBadRequest(Exception e) {
    return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
  }

  /**
   * 处理请求方法不支持异常。
   *
   * @param e 请求方法异常
   * @return 统一错误响应
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    return Result.error(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage());
  }

  /**
   * 处理 Spring Security 认证失败异常。
   *
   * @param e 认证异常
   * @return 统一错误响应
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Result<Void> handleAuthentication(AuthenticationException e) {
    return Result.error(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
  }

  /**
   * 处理 Spring Security 授权失败异常。
   *
   * @param e 授权异常
   * @return 统一错误响应
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Result<Void> handleAccessDenied(AccessDeniedException e) {
    return Result.error(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
  }

  /**
   * 处理未预期系统异常，记录完整堆栈但不向前端暴露内部细节。
   *
   * @param e 系统异常
   * @return 统一错误响应
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleException(Exception e) {
    log.error("Unhandled server exception", e);
    return Result.error(ErrorCode.SYSTEM_ERROR);
  }

  private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case UNAUTHORIZED, TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
      case FORBIDDEN -> HttpStatus.FORBIDDEN;
      case NOT_FOUND,
          USER_NOT_FOUND,
          FAMILY_SPACE_NOT_FOUND,
          FAMILY_MEMBER_NOT_FOUND,
          DEVICE_NOT_FOUND,
          WARRANTY_NOT_FOUND,
          CONSUMABLE_NOT_FOUND,
          MAINTENANCE_NOT_FOUND,
          REMINDER_NOT_FOUND,
          CONFIG_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
      case AI_SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
      default -> HttpStatus.BAD_REQUEST;
    };
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + " " + error.getDefaultMessage();
  }
}
