package es.udc.camisetas.backend.model.exceptions;

public class NoParticipantsException extends Exception {

  private final Long raffleId;

  public NoParticipantsException(Long raffleId) {
    super("El sorteo con id " + raffleId + " no tiene participantes. No puede realizarse el sorteo");
    this.raffleId = raffleId;
  }

    public Long getRaffleId() {
        return raffleId;
    }
}
