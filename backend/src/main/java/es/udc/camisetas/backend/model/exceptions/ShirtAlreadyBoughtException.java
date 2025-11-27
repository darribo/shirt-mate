package es.udc.camisetas.backend.model.exceptions;

public class ShirtAlreadyBoughtException extends Exception {

  private final Long shirtId;

  public ShirtAlreadyBoughtException(Long shirtId) {
    super("La camiseta con id " + shirtId + " ya se ha comprado. No puede comprarse de nuevo");
    this.shirtId = shirtId;
  }

  public Long getShirtId() {
    return shirtId;
  }
}
