package com.fixledger.common.exception;

import com.fixledger.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * 文件功能说明：统一异常组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBusinessException(BusinessException e) {
    return Result.error(e.getErrorCode(), e.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
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
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
    String message = e.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
        .collect(Collectors.joining("; "));
    return Result.error(ErrorCode.BAD_REQUEST, message);
  }

  @ExceptionHandler({
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class
  })
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBadRequest(Exception e) {
    return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    return Result.error(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Result<Void> handleAuthentication(AuthenticationException e) {
    return Result.error(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Result<Void> handleAccessDenied(AccessDeniedException e) {
    return Result.error(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理异常处理。
   * </p>
   * @param e e 参数
   * @return 统一响应结果
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleException(Exception e) {
    log.error("Unhandled server exception", e);
    return Result.error(ErrorCode.SYSTEM_ERROR);
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + " " + error.getDefaultMessage();
  }
}
