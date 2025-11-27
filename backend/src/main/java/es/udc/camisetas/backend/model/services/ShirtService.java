package es.udc.camisetas.backend.model.services;

import es.udc.camisetas.backend.model.entities.*;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.ShirtAlreadyBoughtException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ShirtService {

    ShirtType addShirtType(String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) throws InstanceNotFoundException;

    Block <ShirtType> searchShirtTypes(String name, int page, int size);

    List<ShirtType> getAllShirtTypes();

    ShirtType getShirtType(Long shirtTypeId) throws InstanceNotFoundException;

    ShirtType updateShirtType(Long shirtTypeId, String name, String image, BigDecimal baseSalesPrice, String description, int freeShirtPeople, Long responsibleId) throws InstanceNotFoundException;

    void deleteShirtType(Long shirtTypeId) throws InstanceNotFoundException;

    BigDecimal getShirtTypeRevenue(Long ShirtTypeId) throws InstanceNotFoundException;

    int customersNumber(Long ShirtTypeId) throws InstanceNotFoundException;

    Shirt addShirt(BigDecimal purchasePrice, String size, Long ShirtTypeId, Long investorId, Long customerId) throws InstanceNotFoundException, DuplicateInstanceException;

    Shirt getShirtById(Long shirtId) throws InstanceNotFoundException;

    Shirt updateShirt(Long shirtId, BigDecimal purchasePrice, BigDecimal salePrice, String size, Long shirtTypeId, Long investorId, Long customerId) throws InstanceNotFoundException, DuplicateInstanceException;

    void deleteShirt(Long shirtId) throws InstanceNotFoundException;

    Block <Shirt> searchShirts(String keywords, Long shirtTypeId, Long investorId, String shirtSize, boolean isResponsible, int page, int size);

    List<Shirt> getConvincedFriendsShirt(Long shirtId) throws InstanceNotFoundException;

    int getConvincedFriendsNumber(Long shirtId) throws InstanceNotFoundException;

    int getConvincedFriendsPaid (Long shirtId) throws InstanceNotFoundException;

    Boolean isFree(Long shirtId) throws InstanceNotFoundException; //Mira si una persona puede llevarse la camiseta gratis en función de si sus amigos ya la compraron

    BigDecimal getCurrentShirtPrice(Long shirtId) throws InstanceNotFoundException;

    boolean isBought(Long shirtId) throws InstanceNotFoundException;

    Shirt buy(Long shirtId, BigDecimal salePrice) throws InstanceNotFoundException, ShirtAlreadyBoughtException, DuplicateInstanceException;

    BigDecimal getTotalProfit() throws InstanceNotFoundException;

    void validateCustomer(String instagram, Long shirtTypeId) throws InstanceNotFoundException, DuplicateInstanceException;

    Shirt getShirtByCustomerId(Long customerId) throws InstanceNotFoundException;

    Shirt getShirtByInstagram(String instagram) throws InstanceNotFoundException;
}
