package es.udc.camisetas.backend.model.daos;

import es.udc.camisetas.backend.model.entities.Shirt;
import org.springframework.data.domain.Slice;

public interface CustomizedShirtDao {
    Slice<Shirt> find(String instagram, Long shirtTypeId, Long investorId, String shirtSize, boolean isResponsible, int page, int size);
}
