package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.ShirtType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ShirtTypeDao extends CrudRepository<ShirtType, Long>, ListPagingAndSortingRepository<ShirtType, Long> {

    @Query("""
    SELECT t FROM ShirtType t
    LEFT JOIN Shirt s ON s.shirtType = t
    WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
    GROUP BY t
    ORDER BY COUNT(s.customer) DESC
    """)
    Slice<ShirtType> getByNameOrderByCustomersNumber(@Param("name") String name, Pageable pageable);
}
