package es.udc.camisetas.backend.test.model.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import es.udc.camisetas.backend.model.entities.Collaborator;
import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Raffle;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.entities.Shirt;
import es.udc.camisetas.backend.model.entities.ShirtType;
import es.udc.camisetas.backend.model.enums.Size;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.InvalidPercentageException;
import es.udc.camisetas.backend.model.exceptions.ShirtAlreadyBoughtException;
import es.udc.camisetas.backend.model.services.Block;
import es.udc.camisetas.backend.model.services.CollaboratorService;
import es.udc.camisetas.backend.model.services.CustomerService;
import es.udc.camisetas.backend.model.services.RaffleService;
import es.udc.camisetas.backend.model.services.ShirtService;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ShirtServiceTest {

    private final Long NON_EXISTENT_ID = Long.valueOf(-1);

    @Autowired
    private ShirtService shirtService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CollaboratorService collaboratorService;
    @Autowired
    private RaffleService raffleService;

    @Test
    void testAddShirtTypeAndGetShirtType() throws InstanceNotFoundException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);

        ShirtType foundShirtType = shirtService.getShirtType(shirtType.getId());

        assertEquals(shirtType, foundShirtType);
    }

    @Test
    void testAddShirtTypeAndGetShirtTypeWithResponsible() throws InstanceNotFoundException, DuplicateInstanceException {

        Customer customer = customerService.addCustomer("@darribo1501", null);
        Responsible responsible = customerService.addResponsible("Daniel", "Rivera", "698109250", customer.getId());

        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, responsible.getId());

        ShirtType foundShirtType = shirtService.getShirtType(shirtType.getId());

        assertEquals(shirtType, foundShirtType);

        assertEquals(shirtType.getResponsible(), foundShirtType.getResponsible());
        assertEquals(responsible, foundShirtType.getResponsible());

    }

    @Test
    void testAddShirtTypeWithANotExistentResponsible() {

        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, NON_EXISTENT_ID);
        });
    }

    @Test
    void testGetANotExistentShirtType() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getShirtType(NON_EXISTENT_ID);
        });
    }

    @Test
    void testSearchShirtType() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {
        ShirtType shirtType1 = shirtService.addShirtType("Summer Shirt", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);
        ShirtType shirtType2 = shirtService.addShirtType("Winter Shirt", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);
        ShirtType shirtType3 = shirtService.addShirtType("Summer Hat", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);

        Block<ShirtType> result1 = shirtService.searchShirtTypes("Shirt", 0, 2);
        assertTrue(result1.getItems().contains(shirtType1));
        assertTrue(result1.getItems().contains(shirtType2));
        assertFalse(result1.getItems().contains(shirtType3));
        assertFalse(result1.getExistMoreItems());

        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType3.getId(), collaborator.getId(), customer.getId());

        Block<ShirtType> result2 = shirtService.searchShirtTypes("summer", 0, 1);
        assertFalse(result2.getItems().contains(shirtType1)); //No está porque está en la siguiente página y se ordena por número de clientes
        assertTrue(result2.getItems().contains(shirtType3));
        assertFalse(result2.getItems().contains(shirtType2));
        assertTrue(result2.getExistMoreItems());

        Block<ShirtType> result3 = shirtService.searchShirtTypes("Nonexistent", 0, 2);
        assertTrue(result3.getItems().isEmpty());
        assertFalse(result3.getExistMoreItems());
    }

    @Test
    void updateShirtType() throws InstanceNotFoundException {

        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);
        ShirtType updated = shirtService.updateShirtType(shirtType.getId(), "Ceda el Vaso", shirtType.getImage(), shirtType.getBaseSalesPrice(), shirtType.getDescription(), shirtType.getFreeShirtPeople(), null);
        assertEquals("Ceda el Vaso", updated.getName());
        assertEquals(BigDecimal.valueOf(12.00), updated.getBaseSalesPrice());
        assertEquals(shirtType.getId(), updated.getId());

    }

    @Test
    void updateNotExistentShirtType() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.updateShirtType(NON_EXISTENT_ID, "Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);
        });
    }

    @Test
    void deleteShirtType() throws InstanceNotFoundException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 0, null);

        shirtService.deleteShirtType(shirtType.getId());

        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getShirtType(shirtType.getId());
        });
    }

    @Test
    void deleteNotExistentShirtType() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.deleteShirtType(NON_EXISTENT_ID);
        });
    }

    @Test
    void getShirtTypeRevenueTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@pablitorios", null);
        Customer customer3 = customerService.addCustomer("@dtorbu", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer3.getId());

        assertEquals(shirt1.getProfit().add(shirt2.getProfit()).add(shirt3.getProfit()), shirtService.getShirtTypeRevenue(shirtType.getId()));

        shirtService.buy(shirt1.getId(), shirt1.getPurchasePrice());
        shirtService.buy(shirt2.getId(), shirt2.getPurchasePrice());
        shirtService.buy(shirt3.getId(), shirt3.getPurchasePrice());

        assertEquals(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP), shirtService.getShirtTypeRevenue(shirtType.getId()));

        shirtService.updateShirt(shirt1.getId(), shirt1.getPurchasePrice(), null, shirt1.getSize().toString(), shirt1.getShirtType().getId(), shirt1.getInvestor().getId(), shirt1.getCustomer().getId());

        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(12.00));

        assertEquals(BigDecimal.valueOf(5.00).setScale(1, RoundingMode.HALF_UP), shirtService.getShirtTypeRevenue(shirtType.getId()));

        Raffle raffle = raffleService.addRaffle(BigDecimal.valueOf(1.00), null, shirtType.getId());
        raffleService.addLevel("Nivel 1", BigDecimal.valueOf(8.00), 3, raffle.getId());

        shirtService.updateShirt(shirt1.getId(), shirt1.getPurchasePrice(), null, shirt1.getSize().toString(), shirt1.getShirtType().getId(), shirt1.getInvestor().getId(), shirt1.getCustomer().getId());
        shirtService.updateShirt(shirt2.getId(), shirt1.getPurchasePrice(), null, shirt1.getSize().toString(), shirt1.getShirtType().getId(), shirt1.getInvestor().getId(), shirt1.getCustomer().getId());
        shirtService.updateShirt(shirt3.getId(), shirt1.getPurchasePrice(), null, shirt1.getSize().toString(), shirt1.getShirtType().getId(), shirt1.getInvestor().getId(), shirt1.getCustomer().getId());

        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(13.00));
        shirtService.buy(shirt2.getId(), shirtService.getCurrentShirtPrice(shirt2.getId()));
        shirtService.buy(shirt3.getId(), shirtService.getCurrentShirtPrice(shirt2.getId()));

        assertEquals(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP), shirtService.getShirtTypeRevenue(shirtType.getId()));
    }

    @Test
    void getRevenueOfANotExistentShirtTypeTest() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getShirtTypeRevenue(NON_EXISTENT_ID);
        });
    }

    @Test
    void customersNumberTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@pablitorios", null);
        Customer customer3 = customerService.addCustomer("@dtorbu", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer1.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer3.getId());

        assertEquals(3, shirtService.customersNumber(shirtType.getId()));

    }

    @Test
    void customersNumberOfANotExistentShirtType() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.customersNumber(NON_EXISTENT_ID);
        });
    }

    @Test
    void addShirtAndGetShirtTest() throws InstanceNotFoundException, InvalidPercentageException, DuplicateInstanceException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer.getId());

        Shirt foundShirt = shirtService.getShirtById(shirt.getId());

        assertEquals(shirt, foundShirt);
    }

    @Test
    void addShirtAWithANotExistentShirtType() throws InstanceNotFoundException, InvalidPercentageException, DuplicateInstanceException {
        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.addShirt(BigDecimal.valueOf(7.00), "S", NON_EXISTENT_ID, collaborator.getId(), customer.getId());
        });

    }

    @Test
    void addShirtWithANotExistentCustomer() throws InstanceNotFoundException, InvalidPercentageException, DuplicateInstanceException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), NON_EXISTENT_ID);
        });

    }

    @Test
    void addShirtWithANotExistentInvestor() throws InstanceNotFoundException, InvalidPercentageException, DuplicateInstanceException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer = customerService.addCustomer("@darribo1501", null);

        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), NON_EXISTENT_ID, customer.getId());
        });
    }

    @Test
    void addDuplicateShirt() throws InstanceNotFoundException, InvalidPercentageException, DuplicateInstanceException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        ShirtType shirtType2 = shirtService.addShirtType("Ceda el vaso", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator.getId(), customer.getId());

        assertThrows(DuplicateInstanceException.class, () ->
                shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer.getId()));

        assertThrows(DuplicateInstanceException.class, () ->
                shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator.getId(), customer.getId()));
    }

    @Test
    void updateShirt() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {

        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer.getId());

        Shirt updated = shirtService.updateShirt(shirt.getId(), shirt.getPurchasePrice(), shirt.getSalePrice(), "L", shirt.getShirtType().getId(), shirt.getInvestor().getId(), shirt.getCustomer().getId());
        assertEquals(shirt.getId(), updated.getId());
        assertNotEquals(Size.S, updated.getSize());


    }

    @Test
    void updateNotExistentShirt() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {
        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer = customerService.addCustomer("@darribo1501", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        assertThrows(InstanceNotFoundException.class, () ->
                shirtService.updateShirt(NON_EXISTENT_ID, BigDecimal.valueOf(7.00), BigDecimal.valueOf(12.00), "S", shirtType.getId(), collaborator.getId(), customer.getId()));
    }

    @Test
    void deleteNotExistentShirt() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.deleteShirt(NON_EXISTENT_ID);
        });
    }

    @Test
    void getConvincedFriendsShirtTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {

        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@kokebs", null);
        Customer customer3 = customerService.addCustomer("@pablitorios", customer1.getId());
        Customer customer4 = customerService.addCustomer("@dtorbu", customer1.getId());
        Customer customer5 = customerService.addCustomer("@antiwis", customer1.getId());
        Customer customer6 = customerService.addCustomer("@marcosfdez", customer2.getId());

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer3.getId());
        Shirt shirt4 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer4.getId());
        Shirt shirt5 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer5.getId());
        Shirt shirt6 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer6.getId());

        assertTrue(shirtService.getConvincedFriendsShirt(shirt1.getId()).contains(shirt3));
        assertTrue(shirtService.getConvincedFriendsShirt(shirt1.getId()).contains(shirt4));
        assertTrue(shirtService.getConvincedFriendsShirt(shirt1.getId()).contains(shirt5));
    }

    @Test
    void getConvincedFriendsShirtWithANotExistentShirtTest() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getConvincedFriendsShirt(NON_EXISTENT_ID);
        });
    }

    @Test
    void getConvincedFriendsNumber() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException {
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@kokebs", null);
        Customer customer3 = customerService.addCustomer("@pablitorios", customer1.getId());
        Customer customer4 = customerService.addCustomer("@dtorbu", customer1.getId());
        Customer customer5 = customerService.addCustomer("@antiwis", customer1.getId());
        Customer customer6 = customerService.addCustomer("@marcosfdez", customer2.getId());

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer3.getId());
        Shirt shirt4 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer4.getId());
        Shirt shirt5 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer5.getId());
        Shirt shirt6 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer6.getId());

        assertEquals(3, shirtService.getConvincedFriendsNumber(shirt1.getId()));
    }

    @Test
    void getConvincedFriendsNumberWithANotExistentShirtTest() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getConvincedFriendsNumber(NON_EXISTENT_ID);
        });
    }

    @Test
    void getConvincedFriendsPaidTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@kokebs", null);
        Customer customer3 = customerService.addCustomer("@pablitorios", customer1.getId());
        Customer customer4 = customerService.addCustomer("@dtorbu", customer1.getId());
        Customer customer5 = customerService.addCustomer("@antiwis", customer1.getId());
        Customer customer6 = customerService.addCustomer("@marcosfdez", customer2.getId());

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer3.getId());
        Shirt shirt4 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer4.getId());
        Shirt shirt5 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer5.getId());
        Shirt shirt6 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer6.getId());

        assertEquals(0, shirtService.getConvincedFriendsPaid(shirt1.getId()));

        shirtService.buy(shirt3.getId(), BigDecimal.valueOf(12.00));

        assertEquals(1, shirtService.getConvincedFriendsPaid(shirt1.getId()));
    }

    @Test
    void getConvincedFriendsPaidWithANotExistentShirtTest() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.getConvincedFriendsPaid(NON_EXISTENT_ID);
        });
    }

    @Test
    void isFreeTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {

        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@pablitorios", customer1.getId());
        Customer customer3 = customerService.addCustomer("@dtorbu", customer1.getId());
        Customer customer4 = customerService.addCustomer("@antiwis", customer1.getId());
        Customer customer5 = customerService.addCustomer("@marcosfdez", customer1.getId());

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 3, null);
        ShirtType shirtType2 = shirtService.addShirtType("Ceda el Vaso", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer3.getId());
        Shirt shirt4 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator1.getId(), customer4.getId());
        Shirt shirt5 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer5.getId());

        assertFalse(shirtService.isFree(shirt1.getId()));
        assertFalse(shirtService.isFree(shirt4.getId()));

        shirtService.buy(shirt2.getId(), BigDecimal.valueOf(12.00));
        shirtService.buy(shirt3.getId(), BigDecimal.valueOf(12.00));

        assertFalse(shirtService.isFree(shirt1.getId()));

        shirtService.buy(shirt4.getId(), BigDecimal.valueOf(12.00));
        assertFalse(shirtService.isFree(shirt1.getId())); //Es falso porque uno de los que compró era del tipo2

        shirtService.buy(shirt5.getId(), BigDecimal.valueOf(12.00));
        assertTrue(shirtService.isFree(shirt1.getId()));

        shirtService.updateShirt(shirt5.getId(), shirt5.getPurchasePrice(), null, shirt5.getSize().toString(), shirt5.getShirtType().getId(), shirt5.getInvestor().getId(), shirt5.getCustomer().getId());
        assertFalse(shirtService.isFree(shirt1.getId()));
    }

    @Test
    void isFreeWithANotExistentShirtTest() {
        assertThrows(InstanceNotFoundException.class, () -> {
            shirtService.isFree(NON_EXISTENT_ID);
        });
    }

    @Test
    void getCurrentShirtPriceTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {

        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@pablitorios", customer1.getId());

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 1, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer2.getId());

        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt1.getId()));
        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt2.getId()));

        Raffle raffle = raffleService.addRaffle(BigDecimal.valueOf(1.00), null, shirtType1.getId());

        assertEquals(BigDecimal.valueOf(13.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt1.getId()));
        assertEquals(BigDecimal.valueOf(13.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt2.getId()));

        shirtService.buy(shirt2.getId(), shirtService.getCurrentShirtPrice(shirt2.getId()));

        assertEquals(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt1.getId()).setScale(1, RoundingMode.HALF_UP));
        assertEquals(BigDecimal.valueOf(13.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt2.getId()));

        shirtService.updateShirt(shirt2.getId(), shirt2.getPurchasePrice(), null, shirt2.getSize().toString(), shirt2.getShirtType().getId(), shirt2.getInvestor().getId(), shirt2.getCustomer().getId());

        assertEquals(BigDecimal.valueOf(13.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt1.getId()));
        assertEquals(BigDecimal.valueOf(13.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt2.getId()));

        raffleService.deleteRaffle(raffle.getId());

        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt1.getId()));
        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirtService.getCurrentShirtPrice(shirt2.getId()));

    }

    @Test
    void getCurrentShirtPriceOfANotExistentShirtTest() {
        assertThrows(InstanceNotFoundException.class, () -> shirtService.getCurrentShirtPrice(null));
    }

    @Test
    void buyTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {
        Customer customer1 = customerService.addCustomer("@darribo1501", null);

        ShirtType shirtType1 = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(12.00), null, 1, null);

        Collaborator collaborator1 = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType1.getId(), collaborator1.getId(), customer1.getId());

        assertNull(shirt1.getSalePrice());

        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(12.00));

        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirt1.getSalePrice());

        assertThrows(ShirtAlreadyBoughtException.class, () -> shirtService.buy(shirt1.getId(), BigDecimal.valueOf(12.00)));

        shirtService.updateShirt(shirt1.getId(), shirt1.getPurchasePrice(), null, shirt1.getSize().toString(), shirt1.getShirtType().getId(), shirt1.getInvestor().getId(), shirt1.getCustomer().getId());
        assertNull(shirt1.getSalePrice());

        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(12.00));
        assertEquals(BigDecimal.valueOf(12.00).setScale(1, RoundingMode.HALF_UP), shirt1.getSalePrice());

        assertThrows(ShirtAlreadyBoughtException.class, () -> shirtService.buy(shirt1.getId(), BigDecimal.valueOf(12.00)));

    }

    @Test
    void buyANotExistentTest() {
        assertThrows(InstanceNotFoundException.class, () -> shirtService.buy(null, BigDecimal.valueOf(12.00)));
    }

    @Test
    void getTotalProfitTest() throws InstanceNotFoundException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {

        ShirtType shirtType = shirtService.addShirtType("Garraphone", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        ShirtType shirtType2 = shirtService.addShirtType("Ceda el vaso", "image.jpg", BigDecimal.valueOf(7.00), null, 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@pablitorios", null);
        Customer customer3 = customerService.addCustomer("@dtorbu", null);
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer2.getId());
        Shirt shirt3 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType.getId(), collaborator.getId(), customer3.getId());
        Shirt shirt4 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt5 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator.getId(), customer2.getId());
        Shirt shirt6 = shirtService.addShirt(BigDecimal.valueOf(7.00), "S", shirtType2.getId(), collaborator.getId(), customer3.getId());

        assertEquals(shirt1.getProfit().add(shirt2.getProfit()).add(shirt3.getProfit()).add(shirt4.getProfit()).add(shirt5.getProfit()).add(shirt6.getProfit()), shirtService.getTotalProfit());

        shirtService.buy(shirt1.getId(), shirt1.getPurchasePrice());
        shirtService.buy(shirt2.getId(), shirt2.getPurchasePrice());
        shirtService.buy(shirt3.getId(), shirt3.getPurchasePrice());

        assertEquals(shirt4.getProfit().add(shirt5.getProfit()).add(shirt6.getProfit()), shirtService.getTotalProfit());

        Raffle raffle = raffleService.addRaffle(BigDecimal.valueOf(1.00), null, shirtType2.getId());

        raffleService.addLevel("Nivel 1", BigDecimal.valueOf(3.0), 3, raffle.getId());

        shirtService.buy(shirt4.getId(), shirtService.getCurrentShirtPrice(shirt4.getId()));
        shirtService.buy(shirt5.getId(), shirtService.getCurrentShirtPrice(shirt5.getId()));
        shirtService.buy(shirt6.getId(), shirtService.getCurrentShirtPrice(shirt6.getId()));

        assertEquals(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP), shirtService.getTotalProfit());


    }



}

