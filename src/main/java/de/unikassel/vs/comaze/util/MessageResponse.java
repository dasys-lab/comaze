package de.unikassel.vs.comaze.util;

public class MessageResponse<T> {
  private final String message;
  private final T data;

  public MessageResponse(String message, T data) {
    this.message = message;
    this.data = data;
  }

  public MessageResponse(String message) {
    this(message, null);
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }
}
