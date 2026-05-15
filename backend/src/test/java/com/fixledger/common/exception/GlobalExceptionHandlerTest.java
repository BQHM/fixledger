package com.fixledger.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("业务异常根据错误码返回匹配的 HTTP 状态")
  void businessExceptionUsesHttpStatusByErrorCode() {
    assertBusinessStatus(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    assertBusinessStatus(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    assertBusinessStatus(ErrorCode.DEVICE_NOT_FOUND, HttpStatus.NOT_FOUND);
    assertBusinessStatus(ErrorCode.AI_SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @Test
  @DisplayName("请求解析异常返回通用错误信息")
  void badRequestDoesNotExposeInternalExceptionMessage() {
    Result<Void> response = handler.handleBadRequest(
        new HttpMessageNotReadableException("JSON parse error: internal stack detail")
    );

    assertThat(response.code()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());
    assertThat(response.message()).isEqualTo("请求参数格式不正确");
  }

  private void assertBusinessStatus(ErrorCode errorCode, HttpStatus expectedStatus) {
    ResponseEntity<Result<Void>> response = handler.handleBusinessException(
        new BusinessException(errorCode)
    );

    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo(errorCode.getCode());
    assertThat(response.getBody().message()).isEqualTo(errorCode.getMessage());
  }
}
