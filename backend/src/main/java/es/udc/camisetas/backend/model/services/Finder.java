package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.*;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;

public interface Finder {

    Customer findCustomerById(Long customerId) throws InstanceNotFoundException;

    Collaborator findCollaboratorById(Long collaboratorId) throws InstanceNotFoundException;

    Responsible findResponsibleById(Long responsibleId) throws InstanceNotFoundException;

    ShirtType findByShirtTypeId(Long shirtTypeId) throws InstanceNotFoundException;

    Raffle findRaffleById(Long raffleId) throws InstanceNotFoundException;

    Shirt findShirtById(Long shirtId) throws InstanceNotFoundException;

    Level findLevelById(Long levelId) throws InstanceNotFoundException;
}
