package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Raffle;
import es.udc.camisetas.backend.model.entities.Level;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.NoParticipantsException;

import java.math.BigDecimal;

public interface RaffleService {

    Raffle addRaffle(BigDecimal participationPrice, String description, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException;

    Raffle getRaffle(Long raffleId) throws InstanceNotFoundException;

    Raffle updateRaffle(Long raffleId, BigDecimal participationPrice, String description, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException;

    void deleteRaffle(Long raffleId) throws InstanceNotFoundException;

    int getParticipantsNumber(Long raffleId) throws InstanceNotFoundException;

    BigDecimal getRafflePrice(Long raffleId) throws InstanceNotFoundException;

    Raffle getRaffleByShirtTypeId(Long shirtTypeId) throws InstanceNotFoundException;

    boolean existsRaffleByShirtTypeId(Long shirtTypeId);

    Level addLevel(String description, BigDecimal price, int neccessaryParticipants, Long raffleId) throws InstanceNotFoundException;

    Level getLevel(Long levelId) throws InstanceNotFoundException;

    Level updateLevel(Long levelId, String description, BigDecimal price, int neccessaryParticipants) throws InstanceNotFoundException;

    void removeLevel(Long levelId) throws InstanceNotFoundException;

    boolean levelRaised(Long levelId) throws InstanceNotFoundException;

    Level play(Long levelId) throws InstanceNotFoundException, NoParticipantsException;
}
