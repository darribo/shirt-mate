package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Level;
import org.springframework.data.repository.CrudRepository;

public interface LevelDao extends CrudRepository<Level, Long> {

    boolean existsLevelByRaffleIdAndWinnerId(Long raffleId, Long winnerId);
}
