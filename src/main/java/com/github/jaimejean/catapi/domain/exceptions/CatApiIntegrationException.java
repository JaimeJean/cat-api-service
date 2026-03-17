package com.github.jaimejean.catapi.domain.exceptions;

public class CatApiIntegrationException extends RuntimeException {

  public CatApiIntegrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CatApiIntegrationException(String message) {
    super(message);
  }
}
