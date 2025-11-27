package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Responsible;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ResponsibleDao extends CrudRepository<Responsible, Long> {
    Optional<Responsible> getResponsibleByCustomerId(Long customerId);
}
