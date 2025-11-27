package es.udc.camisetas.backend.test.model.services;

import es.udc.camisetas.backend.model.entities.*;
import es.udc.camisetas.backend.model.exceptions.*;
import es.udc.camisetas.backend.model.services.CollaboratorService;
import es.udc.camisetas.backend.model.services.CustomerService;
import es.udc.camisetas.backend.model.services.RaffleService;
import es.udc.camisetas.backend.model.services.ShirtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RaffleServiceTest {

    private final Long NON_EXISTENT_ID = Long.valueOf(-1);

    @Autowired
    private RaffleService raffleService;

    @Autowired
    private ShirtService shirtService;

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private CustomerService customerService;

    private ShirtType createShirtType() throws InstanceNotFoundException {
        return shirtService.addShirtType("Modelo A", "imagen.jpg",
                new BigDecimal("10.00"), "Descripción", 0, null);
    }

    private ShirtType addShirts() {
        try {
            Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
            ShirtType shirtType = createShirtType();
            Customer customer1 = customerService.addCustomer("@darribo1501", null);
            Customer customer2 = customerService.addCustomer("@dtorbu", null);
            Customer customer3 = customerService.addCustomer("@wiwis", null);
            Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
            Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
            shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer3.getId());
            shirtService.buy(shirt1.getId(), BigDecimal.valueOf(13));
            shirtService.buy(shirt2.getId(), BigDecimal.valueOf(13));

            return shirtType;

        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testAddAndGetRaffle() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType = createShirtType();
        Raffle added = raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo de prueba", shirtType.getId());

        Raffle found = raffleService.getRaffle(added.getId());

        assertEquals(added.getId(), found.getId());
        assertEquals("Sorteo de prueba", found.getDescription());
        assertEquals(new BigDecimal("2.00"), found.getParticipationPrice());
        assertEquals(shirtType.getId(), found.getShirtType().getId());
    }

    @Test
    public void testAddDuplicateRaffle() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType = createShirtType();

        raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo duplicado", shirtType.getId());

        assertThrows(DuplicateInstanceException.class, () ->
                raffleService.addRaffle(new BigDecimal("3.00"), "Otro sorteo", shirtType.getId()));
    }

    @Test
    public void testGetNonExistentRaffle() {
        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.getRaffle(NON_EXISTENT_ID));
    }

    @Test
    public void testUpdateRaffle() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType1 = createShirtType();
        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Inicial",shirtType1.getId());

        ShirtType shirtType2 = shirtService.addShirtType("Modelo B", "img2.jpg",
                new BigDecimal("12.00"), "Desc B", 1, null);

        Raffle updated = raffleService.updateRaffle(raffle.getId(), new BigDecimal("3.00"), "Actualizado",
                shirtType2.getId());

        assertEquals("Actualizado", updated.getDescription());
        assertEquals(new BigDecimal("3.00"), updated.getParticipationPrice());
        assertEquals(shirtType2.getId(), updated.getShirtType().getId());
    }

    @Test
    public void testUpdateToDuplicateRaffle() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType1 = createShirtType();
        ShirtType shirtType2 = shirtService.addShirtType("Modelo C", "img3.jpg",
                new BigDecimal("15.00"), "Desc C", 2, null);

        raffleService.addRaffle(new BigDecimal("1.50"), "Raffle A", shirtType1.getId());
        Raffle raffleB = raffleService.addRaffle(new BigDecimal("1.75"), "Raffle B", shirtType2.getId());

        assertThrows(DuplicateInstanceException.class, () ->
                raffleService.updateRaffle(raffleB.getId(), new BigDecimal("2.00"), "New", shirtType1.getId()));
    }

    @Test
    public void testDeleteRaffle() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType = createShirtType();
        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Para borrar",
                shirtType.getId());

        raffleService.deleteRaffle(raffle.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.getRaffle(raffle.getId()));
    }

    @Test
    public void testDeleteNonExistentRaffle() {
        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.deleteRaffle(NON_EXISTENT_ID));
    }

    @Test
    public void getParticipantsNumberTest() throws InstanceNotFoundException, NoParticipantsException, DuplicateInstanceException {
        ShirtType shirtType = addShirts();
        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo de prueba", shirtType.getId());
        assertEquals(2, raffleService.getParticipantsNumber(raffle.getId()));
    }

    // --- Helper ---
    private Raffle createRaffleWithShirtType() throws InstanceNotFoundException, DuplicateInstanceException {
        ShirtType shirtType = shirtService.addShirtType(
                "Modelo X", "img.jpg",
                new BigDecimal("10.00"), "Descripción", 0, null);
        return raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo test", shirtType.getId());
    }

    // --- Test add + get ---
    @Test
    public void testAddAndGetLevel() throws InstanceNotFoundException, DuplicateInstanceException {
        Raffle raffle = createRaffleWithShirtType();
        Level added = raffleService.addLevel("Primer nivel", new BigDecimal("20.00"), 50, raffle.getId());

        Level found = raffleService.getLevel(added.getId());

        assertEquals(added.getId(), found.getId());
        assertEquals("Primer nivel", found.getLevelDescription());
        assertEquals(new BigDecimal("20.00"), found.getPrice());
        assertEquals(50, found.getNecessaryParticipants());
        assertEquals(raffle.getId(), found.getRaffle().getId());
    }

    // --- Test get non-existent ---
    @Test
    public void testGetNonExistentLevel() {
        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.getLevel(NON_EXISTENT_ID));
    }

    // --- Test update ---
    @Test
    public void testUpdateLevel() throws InstanceNotFoundException, DuplicateInstanceException {
        Raffle raffle = createRaffleWithShirtType();
        Level level = raffleService.addLevel("Inicial", new BigDecimal("10.00"), 10, raffle.getId());

        Level updated = raffleService.updateLevel(level.getId(), "Actualizado", new BigDecimal("15.00"), 20);

        assertEquals(level.getId(), updated.getId());
        assertEquals("Actualizado", updated.getLevelDescription());
        assertEquals(new BigDecimal("15.00"), updated.getPrice());
        assertEquals(20, updated.getNecessaryParticipants());
    }

    // --- Test update non-existent ---
    @Test
    public void testUpdateNonExistentLevel() {
        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.updateLevel(NON_EXISTENT_ID, "X", new BigDecimal("1.00"), 1));
    }

    // --- Test delete ---
    @Test
    public void testRemoveLevel() throws InstanceNotFoundException, DuplicateInstanceException {
        Raffle raffle = createRaffleWithShirtType();
        Level level = raffleService.addLevel("A borrar", new BigDecimal("5.00"), 5, raffle.getId());

        raffleService.removeLevel(level.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.getLevel(level.getId()));
    }

    // --- Test delete non-existent ---
    @Test
    public void testRemoveNonExistentLevel() {
        assertThrows(InstanceNotFoundException.class, () ->
                raffleService.removeLevel(NON_EXISTENT_ID));
    }

    @Test
    public void levelRaisedTest() throws InvalidPercentageException, InstanceNotFoundException, DuplicateInstanceException, ShirtAlreadyBoughtException {
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        ShirtType shirtType = createShirtType();
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@dtorbu", null);

        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo de prueba", shirtType.getId());
        Level level1 = raffleService.addLevel("Nivel1", new BigDecimal("10.00"), 1, raffle.getId());
        Level level2 = raffleService.addLevel("Nivel2", new BigDecimal("10.00"), 2, raffle.getId());
        Level level3 = raffleService.addLevel("Nivel3", new BigDecimal("10.00"), 3, raffle.getId());

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(13));
        assertTrue(raffleService.levelRaised(level1.getId()));
        assertFalse(raffleService.levelRaised(level2.getId()));
        assertFalse(raffleService.levelRaised(level3.getId()));

        shirtService.buy(shirt2.getId(), BigDecimal.valueOf(13));
        assertTrue(raffleService.levelRaised(level2.getId()));
        assertFalse(raffleService.levelRaised(level3.getId()));
    }

    @Test
    public void getRafflePriceTest() throws InvalidPercentageException, InstanceNotFoundException, DuplicateInstanceException, ShirtAlreadyBoughtException {
        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        ShirtType shirtType = shirtService.addShirtType("Modelo A", "imagen.jpg",
                new BigDecimal("10.00"), "Descripción", 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@dtorbu", null);

        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo de prueba", shirtType.getId());
        raffleService.addLevel("Nivel1", new BigDecimal("5.00"), 1, raffle.getId());
        raffleService.addLevel("Nivel2", new BigDecimal("3.00"), 2, raffle.getId());
        raffleService.addLevel("Nivel3", new BigDecimal("2.00"), 3, raffle.getId());

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.buy(shirt1.getId(), shirtService.getCurrentShirtPrice(shirt1.getId()));

        assertEquals(BigDecimal.valueOf(5.00).setScale(2, RoundingMode.HALF_UP), raffleService.getRafflePrice(raffle.getId()));

        shirtService.buy(shirt2.getId(), shirtService.getCurrentShirtPrice(shirt2.getId()));
        assertEquals(BigDecimal.valueOf(8.00).setScale(2, RoundingMode.HALF_UP), raffleService.getRafflePrice(raffle.getId()));
    }

    @Test
    public void playTest() throws InstanceNotFoundException, NoParticipantsException, DuplicateInstanceException, InvalidPercentageException, ShirtAlreadyBoughtException {

        Collaborator collaborator = collaboratorService.addCollaborator("Daniel", BigDecimal.valueOf(50.00));
        ShirtType shirtType = createShirtType();
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@dtorbu", null);

        Raffle raffle = raffleService.addRaffle(new BigDecimal("2.00"), "Sorteo de prueba", shirtType.getId());
        Level level1 = raffleService.addLevel("Nivel1", new BigDecimal("10.00"), 1, raffle.getId());
        Level level2 = raffleService.addLevel("Nivel2", new BigDecimal("10.00"), 2, raffle.getId());
        Level level3 = raffleService.addLevel("Nivel3", new BigDecimal("10.00"), 3, raffle.getId());

        assertThrows(NoParticipantsException.class, () ->
                raffleService.play(level1.getId()));

        Shirt shirt1 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
        Shirt shirt2 = shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.buy(shirt1.getId(), BigDecimal.valueOf(13));
        shirtService.buy(shirt2.getId(), BigDecimal.valueOf(13));

        assertNull(level1.getWinner());
        assertNull(level2.getWinner());

        raffleService.play(level1.getId());
        assertNotNull(level1.getWinner());
        assertNull(level2.getWinner());
        System.out.println("El ganador es: " + level1.getWinner().getInstagram());

        raffleService.play(level2.getId());
        assertNotNull(level1.getWinner());
        assertNotNull(level2.getWinner());
        assertNotEquals(level1.getWinner().getInstagram(), level2.getWinner().getInstagram());

        assertThrows(NoParticipantsException.class, () ->
                raffleService.play(level3.getId()));
    }

}
