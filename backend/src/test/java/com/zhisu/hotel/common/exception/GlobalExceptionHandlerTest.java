package com.zhisu.hotel.common.exception;

import com.zhisu.hotel.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldUseDefaultBusinessErrorCode() {
        BusinessException exception =
                new BusinessException("Business failed");

        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.BUSINESS_ERROR);
    }

    @Test
    void shouldPreserveCustomBusinessErrorCode() {
        BusinessException exception =
                new BusinessException("ROOM_NOT_FOUND", "Room not found");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ROOM_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Room not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleMissingRequestParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "page",
                        "Integer"
                );

        ApiResponse<Void> response =
                handler.handleMissingParameterException(exception);

        assertThat(response.getCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getMessage())
                .isEqualTo("Missing required parameter: page");
    }

    @Test
    void shouldHandleMalformedRequestBody() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Malformed JSON",
                        new MockHttpInputMessage(new byte[0])
                );

        ApiResponse<Void> response =
                handler.handleMessageNotReadableException(exception);

        assertThat(response.getCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(response.getMessage())
                .isEqualTo("Malformed request body");
    }

    @Test
    void shouldHandleUnsupportedRequestMethod() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PATCH");

        ApiResponse<Void> response =
                handler.handleMethodNotSupportedException(exception);

        assertThat(response.getCode())
                .isEqualTo(ErrorCode.METHOD_NOT_ALLOWED);
        assertThat(response.getMessage())
                .isEqualTo("Request method not supported");
    }

    @Test
    void shouldHandleMissingResource() {
        NoResourceFoundException exception =
                new NoResourceFoundException(
                        HttpMethod.GET,
                        "/missing"
                );

        ApiResponse<Void> response =
                handler.handleResourceNotFoundException(exception);

        assertThat(response.getCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(response.getMessage())
                .isEqualTo("Resource not found");
    }

    @Test
    void shouldHandleDataConflict() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Duplicate record"
                );

        ApiResponse<Void> response =
                handler.handleDataIntegrityViolationException(exception);

        assertThat(response.getCode()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(response.getMessage()).isEqualTo("Data conflict");
    }

    @Test
    void shouldReturnUnauthorizedForAuthenticationBusinessErrors() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BusinessException("REFRESH_TOKEN_INVALID", "Session expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("REFRESH_TOKEN_INVALID");
    }

    @Test
    void shouldReturnForbiddenForPermissionBusinessErrors() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(
                new BusinessException("FORBIDDEN", "Access denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}