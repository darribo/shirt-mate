package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.enums.Size;
import es.udc.camisetas.backend.model.entities.Shirt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public class CustomizedShirtDaoImpl implements CustomizedShirtDao {

    //Anotación que permite inyectar la dependencia de EntityManager
    @PersistenceContext

    //Entidad que permite realizar operaciones CRUD y, como para el find las necesitas, necesitas la entidad.
    private EntityManager entityManager;

    /*SELECT * FROM Shirt t
    JOIN Customer c
    ON t.CustomerId = c.Id
    JOIN Responsible r
    on c.Id = r.Id
    where c.Name LIKE keywords
    and t.ShirtTypeId = shirtTypeId
    and t.investorId = investorId
    and t.size = size*/


    @Override
    public Slice<Shirt> find(String instagram, Long shirtTypeId, Long investorId, String shirtSize, boolean isResponsible, int page, int size) {

        StringBuilder queryString = new StringBuilder("SELECT t FROM Shirt t JOIN t.customer c");
        StringBuilder where = new StringBuilder();

        if (isResponsible) {
            where.append(" EXISTS (SELECT 1 FROM Responsible r WHERE r.customer.id = c.id) ");
        }

        if (shirtTypeId != null) {
            if (!where.isEmpty()) where.append(" AND ");
            where.append(" t.shirtType.id = :shirtTypeId");
        }

        if (investorId != null) {
            if (!where.isEmpty()) where.append(" AND ");
            where.append(" t.investor.id = :investorId");
        }

        if (instagram != null) {
            if (!where.isEmpty()) where.append(" AND ");
            where.append(" LOWER(c.instagram) LIKE LOWER(CONCAT('%', :instagram, '%'))");
        }

        if (shirtSize != null) {
            if (!where.isEmpty()) where.append(" AND ");
            where.append(" t.size = :shirtSize");
        }

        if (!where.isEmpty()) {
            queryString.append(" WHERE ").append(where);
        }

        queryString.append(
                " ORDER BY " +
                        " CASE WHEN t.salePrice IS NULL THEN 0 ELSE 1 END, " + // No vendidos (NULL) después
                        " c.instagram ASC, " +
                        " t.shirtType.id ASC"
        );


        Query query = entityManager.createQuery(queryString.toString());
        if (shirtTypeId != null) query.setParameter("shirtTypeId", shirtTypeId);
        if (investorId != null) query.setParameter("investorId", investorId);
        if (instagram != null) query.setParameter("instagram", instagram);
        if (shirtSize != null) query.setParameter("shirtSize", Size.valueOf(shirtSize));

        query.setFirstResult(page * size);
        query.setMaxResults(size + 1);

        List<Shirt> shirts = query.getResultList();
        boolean hasNext = shirts.size() == size + 1;
        if (hasNext) shirts.remove(shirts.size() - 1);

        return new SliceImpl<>(shirts, PageRequest.of(page, size), hasNext);
    }

}
