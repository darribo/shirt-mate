package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Collaborator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CollaboratorDao extends CrudRepository<Collaborator,Long>, ListPagingAndSortingRepository<Collaborator, Long> {

    @Query("""
    SELECT c FROM Collaborator c
    WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))
    ORDER BY c.profitPercentage DESC
    """)
    Slice<Collaborator> findByNameOrderByProfitPercentage(@Param("name") String name, Pageable pageable);
}
