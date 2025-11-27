package es.udc.camisetas.backend.test.model.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import es.udc.camisetas.backend.model.entities.Collaborator;
import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.ShirtType;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.exceptions.InvalidPercentageException;
import es.udc.camisetas.backend.model.services.CollaboratorService;
import es.udc.camisetas.backend.model.services.CustomerService;
import es.udc.camisetas.backend.model.services.RaffleService;
import es.udc.camisetas.backend.model.services.ShirtService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CollaboratorServiceTest {

    private final Long NON_EXISTENT_ID = -1L;

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private ShirtService shirtService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RaffleService raffleService;

    private Collaborator createCollaborator(String name, BigDecimal profitPercentage) {
        try {
            return collaboratorService.addCollaborator(name, profitPercentage);
        } catch (InvalidPercentageException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAddAndGetCollaborator() throws InstanceNotFoundException, InvalidPercentageException {
        Collaborator collaborator = collaboratorService.addCollaborator("Juan", new BigDecimal("20.5"));

        Collaborator found = collaboratorService.getCollaborator(collaborator.getId());

        assertEquals(collaborator.getId(), found.getId());
        assertEquals("Juan", found.getName());
        assertEquals(new BigDecimal("20.5"), found.getProfitPercentage());
    }

    @Test
    public void testAddWithInvalidPercentage() throws InstanceNotFoundException, InvalidPercentageException {
        collaboratorService.addCollaborator("Juan", new BigDecimal("25"));
        collaboratorService.addCollaborator("Juan", new BigDecimal("25"));
        collaboratorService.addCollaborator("Juan", new BigDecimal("25"));
        collaboratorService.addCollaborator("Juan", new BigDecimal("25"));

        assertThrows(InvalidPercentageException.class, () ->
                collaboratorService.addCollaborator("Juan", new BigDecimal("1")));
    }



    @Test
    public void testGetNonExistentCollaborator() {
        assertThrows(InstanceNotFoundException.class, () ->
                collaboratorService.getCollaborator(NON_EXISTENT_ID));
    }

    @Test
    public void testUpdateCollaborator() throws InstanceNotFoundException, InvalidPercentageException {
        Collaborator collaborator = createCollaborator("Lucía", new BigDecimal("15.00"));

        Collaborator updated = collaboratorService.updateCollaborator(
                collaborator.getId(), "Lucía Gómez", new BigDecimal("30.00"));

        assertEquals(collaborator.getId(), updated.getId());
        assertEquals("Lucía Gómez", updated.getName());
        assertEquals(new BigDecimal("30.00"), updated.getProfitPercentage());
    }

    @Test
    public void testDeleteCollaborator() throws InstanceNotFoundException {
        Collaborator collaborator = createCollaborator("Marta", new BigDecimal("10.00"));

        collaboratorService.deleteCollaborator(collaborator.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                collaboratorService.getCollaborator(collaborator.getId()));
    }

    @Test
    public void testDeleteNonExistentCollaborator() {
        assertThrows(InstanceNotFoundException.class, () ->
                collaboratorService.deleteCollaborator(NON_EXISTENT_ID));
    }

    @Test
    public void getNumberOfBoughtShirtsTest() throws InstanceNotFoundException, DuplicateInstanceException {
        Collaborator collaborator = createCollaborator("Ana", new BigDecimal("10.00"));
        ShirtType shirtType = shirtService.addShirtType("Modelo A", "imagen.jpg",
                new BigDecimal("10.00"), "Descripción", 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@dtorbu", null);
        Customer customer3 = customerService.addCustomer("@wiwis", null);
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer3.getId());

        assertEquals(3, collaboratorService.getNumberOfBoughtShirts(collaborator.getId()));
    }

    @Test
    public void getInvestmentTest() throws InstanceNotFoundException, DuplicateInstanceException {
        Collaborator collaborator = createCollaborator("Ana", new BigDecimal("10.00"));
        ShirtType shirtType = shirtService.addShirtType("Modelo A", "imagen.jpg",
                new BigDecimal("10.00"), "Descripción", 0, null);
        Customer customer1 = customerService.addCustomer("@darribo1501", null);
        Customer customer2 = customerService.addCustomer("@dtorbu", null);
        Customer customer3 = customerService.addCustomer("@wiwis", null);
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer1.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer2.getId());
        shirtService.addShirt(BigDecimal.valueOf(7.00), "L", shirtType.getId(), collaborator.getId(), customer3.getId());

        assertEquals((BigDecimal.valueOf(7.0).multiply(BigDecimal.valueOf(3.0))).setScale(1, RoundingMode.HALF_UP), (collaboratorService.getInvestment(collaborator.getId())).setScale(1, RoundingMode.HALF_UP));
    }
}
