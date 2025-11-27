package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.Collaborator;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.InvalidPercentageException;

import java.math.BigDecimal;
import java.util.List;

public interface CollaboratorService {

    boolean validatePercentage(Long id, BigDecimal profitPercentage);

    Collaborator addCollaborator(String name, BigDecimal profitPercentage) throws InvalidPercentageException;

    Block <Collaborator> searchCollaborators(String name, int page, int size);

    List<Collaborator> getAllCollaborators();

    Collaborator getCollaborator(Long collaboratorId) throws InstanceNotFoundException;

    Collaborator updateCollaborator(Long collaboratorId, String name, BigDecimal profitPercentage) throws InstanceNotFoundException, InvalidPercentageException;

    void deleteCollaborator(Long collaboratorId) throws InstanceNotFoundException;

    int getNumberOfBoughtShirts(Long collaboratorId) throws InstanceNotFoundException;

    BigDecimal getInvestment(Long collaboratorId) throws InstanceNotFoundException;

    BigDecimal returnedInvestment(Long collaboratorId) throws InstanceNotFoundException;

    //Todo: Probar
    BigDecimal getProfit(Long collaboratorId) throws InstanceNotFoundException;

}
