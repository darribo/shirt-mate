package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Raffle;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface RaffleDao extends CrudRepository <Raffle, Long> {
    boolean existsRaffleByShirtTypeId(Long shirtTypeId);

    Optional<Raffle> getRaffleByShirtTypeId(Long shirtTypeId);
}
