package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerDao extends CrudRepository<Customer, Long> {

    boolean existsByInstagram(String instagram);

    Optional<Customer> findCustomerByInstagram(String instagram);

}
