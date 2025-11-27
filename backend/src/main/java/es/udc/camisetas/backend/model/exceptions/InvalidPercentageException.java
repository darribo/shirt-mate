package es.udc.camisetas.backend.model.exceptions;

public class InvalidPercentageException extends Exception {

    public InvalidPercentageException() {
        super("Porcentaje inválido: La suma de porcentajes tiene que dar 100%");
    }
}
