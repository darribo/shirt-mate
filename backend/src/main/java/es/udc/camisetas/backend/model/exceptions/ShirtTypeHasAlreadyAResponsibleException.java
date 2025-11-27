package es.udc.camisetas.backend.model.exceptions;

public class ShirtTypeHasAlreadyAResponsibleException extends Exception{
    private final Long shirtTypeId;

    public ShirtTypeHasAlreadyAResponsibleException(Long shirtTypeId) {
        super("La peña con id " + shirtTypeId + " ya tiene responsable.");
        this.shirtTypeId = shirtTypeId;
    }

    public Long getShirtTypeId() {
        return shirtTypeId;
    }
}
