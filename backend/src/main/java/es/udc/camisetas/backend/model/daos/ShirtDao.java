package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Shirt;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ShirtDao extends CrudRepository<Shirt, Long>, CustomizedShirtDao {

    List<Shirt> findAllByShirtTypeId(Long tShirtTypeId);

    Optional<Shirt> findShirtByShirtTypeIdAndCustomerId(Long id, Long id1);

    Slice<Shirt> getShirtsByInvestorId(Long investorId);

    int countShirtsByShirtTypeId(Long tShirtTypeId);

    boolean existsShirtByShirtTypeIdAndCustomerInstagram(Long id, String instagram);

    Optional<Shirt> getShirtByCustomerId(Long customerId);

    Optional<Shirt> getShirtByCustomerInstagram(String instagram);
}
