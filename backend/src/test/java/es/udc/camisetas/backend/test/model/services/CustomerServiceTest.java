package es.udc.camisetas.backend.test.model.services;

import es.udc.camisetas.backend.model.daos.CustomerDao;
import es.udc.camisetas.backend.model.entities.Customer;
import es.udc.camisetas.backend.model.entities.Responsible;
import es.udc.camisetas.backend.model.exceptions.DuplicateInstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceException;
import es.udc.camisetas.backend.model.exceptions.InstanceNotFoundException;
import es.udc.camisetas.backend.model.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class CustomerServiceTest {

    private final Long NON_EXISTENT_ID = Long.valueOf(-1);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerDao customerDao;


    private Customer createCustomer(String instagram) {
        Customer customer = new Customer(instagram);

        try{
            customer = customerService.addCustomer(customer.getInstagram(), null);
            return customer;
        } catch (InstanceException e) {
            throw new RuntimeException(e);
        }
    }

    private Responsible createResponsible(String name, String surname, String phoneNumber, Long customerId) {

        try {
            return customerService.addResponsible(name, surname, phoneNumber, customerId);
        } catch (InstanceException e) {
            throw new RuntimeException(e);
        }
    }

    //CUSTOMER

    //Prueba la correcta ejecución de añadir un cliente y buscarlo por Id
    @Test
    public void testAddCustomerAndGetCustomer() throws InstanceNotFoundException {

        Customer addedCustomer = new Customer("@darribo1501");

        addedCustomer = customerService.addCustomer(addedCustomer.getInstagram(), (addedCustomer.getConvincingFriend() != null ? addedCustomer.getConvincingFriend().getId() : null));

        Customer foundCustomer = customerService.getCustomer(addedCustomer.getId());

        assertEquals(addedCustomer, foundCustomer);

    }

    @Test
    public void testGetCustomerByInstagram() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");

        Customer found = customerService.getCustomerByInstagram(customer.getInstagram());

        assertEquals(customer, found);

        assertThrows(InstanceNotFoundException.class, () -> customerService.getCustomerByInstagram("darribo1501"));
    }

    //Prueba la correcta ejecución de buscar un cliente inexistente
    @Test
    public void testFindANotExistentCustomer() {

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getCustomer(NON_EXISTENT_ID));

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getCustomer(null));

    }

    //Prueba la correcta ejecución de añadir un cliente con convencedor y buscarlo por Id
    @Test
    public void testAddCustomerAndGetCustomerWithConvincingFriend() throws InstanceNotFoundException {

        Customer addedCustomer = new Customer("@pablitorios");
        Customer convincingFriend = new Customer("darribo1501");

        convincingFriend = customerService.addCustomer(convincingFriend.getInstagram(), null);

        addedCustomer = customerService.addCustomer(addedCustomer.getInstagram(), convincingFriend.getId());

        Customer foundConvincingFriend = customerService.getCustomer(convincingFriend.getId());

        assertEquals(addedCustomer.getConvincingFriend(), foundConvincingFriend);
    }

    //Prueba la correcta ejecución de añadir un cliente con un convencedor que no existe
    @Test
    public void testAddCustomerAndGetCustomerWithANotExistentConvincingFriend() {

        Customer addedCustomer = new Customer("@pablitorios");

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.addCustomer(addedCustomer.getInstagram(), NON_EXISTENT_ID));
    }

    //Prueba la correcta ejecución de actualizar un cliente
    @Test
    public void testUpdateCustomer() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");

        Customer auxCustomer = new Customer(customer.getInstagram());
        auxCustomer.setId(customer.getId());

        customer = customerService.updateCustomer(customer.getId(), "@pablitorioss", null);

        assertEquals(customer.getId(), auxCustomer.getId());
        assertNotEquals(customer.getInstagram(), auxCustomer.getInstagram());
        assertEquals(customer.getConvincingFriend(), auxCustomer.getConvincingFriend());

        Customer convincingFriend = customerService.addCustomer("@dati1609", null);

        customer = customerService.updateCustomer(customer.getId(), "@pablitorioss", convincingFriend.getId());

        assertNotEquals(customer.getConvincingFriend(), auxCustomer.getConvincingFriend());
        assertEquals(customer.getConvincingFriend(), convincingFriend);

        Customer convincingFriend2 = customerService.addCustomer("@xabireymendez", null);

        customer = customerService.updateCustomer(customer.getId(), "@pablitorioss", convincingFriend2.getId());

        assertEquals(customer.getConvincingFriend(), convincingFriend2);

        customer = customerService.updateCustomer(customer.getId(), "@pablitorioss", null);

        assertNull(customer.getConvincingFriend());
        assertEquals(customer.getConvincingFriend(), auxCustomer.getConvincingFriend());

    }

    //Prueba la correcta ejecución de intentar modificar un cliente que no existe
    @Test
    public void testUpdateANotExistentCustomer() {

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.updateCustomer(NON_EXISTENT_ID, "@darribo1501", null));

    }

    //Prueba la correcta ejecución de intentar modificar un cliente que no existe
    @Test
    public void testUpdateWithANotExistentConvincingFriend() {

        Customer customer = createCustomer("@darribo1501");

        assertThrows(InstanceNotFoundException.class, () ->
                    customerService.updateCustomer(customer.getId(), "@darribo1501", NON_EXISTENT_ID));

    }

    //Prueba la correcta ejecución de borrar un cliente
    @Test
    public void testDeleteCustomer() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");

        customerService.deleteCustomer(customer.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getCustomer(customer.getId()));
    }

    //Prueba la correcta ejecución de intentar borrar un cliente inexistente
    @Test
    public void testDeleteANotExistentCustomer() {

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteCustomer(NON_EXISTENT_ID));
    }

    //Prueba la correcta ejecución de borrar a un amigo convencido
    @Test
    public void testDeleteConvincedFriend() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");
        Customer customer2 = customerService.addCustomer("@pablitorios", customer.getId());
        Customer customer3 = customerService.addCustomer("@xabireymendez", customer.getId());

        Set<Customer> convincedFriends = new HashSet<>();
        convincedFriends.add(customer3);
        convincedFriends.add(customer2);

        assertEquals(
                convincedFriends.stream().sorted(Comparator.comparing(Customer::getId)).toList(),
                customer.getConvincedFriends().stream().sorted(Comparator.comparing(Customer::getId)).toList()
        );
        assertEquals(customer, customer2.getConvincingFriend());
        assertEquals(customer, customer3.getConvincingFriend());

        customerService.deleteConvincedFriend(customer.getId(), customer2.getId());

        convincedFriends.remove(customer2);

        assertEquals(
                convincedFriends.stream().sorted(Comparator.comparing(Customer::getId)).toList(),
                customer.getConvincedFriends().stream().sorted(Comparator.comparing(Customer::getId)).toList()
        );

        assertNull(customer2.getConvincingFriend());
        assertEquals(customer, customer3.getConvincingFriend());

        customerService.deleteConvincedFriend(customer.getId(), customer3.getId());
        convincedFriends.remove(customer3);

        assertEquals(
                convincedFriends.stream().sorted(Comparator.comparing(Customer::getId)).toList(),
                customer.getConvincedFriends().stream().sorted(Comparator.comparing(Customer::getId)).toList()
        );

        assertNull(customer3.getConvincingFriend());
    }

    //Prueba la correcta ejecución de borrar a un amigo convencido de un cliente inexistente
    @Test
    public void testDeleteConvincedFriendFromANotExistentCustomer() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");
        Customer customer2 = customerService.addCustomer("@pablitorios", customer.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteConvincedFriend(NON_EXISTENT_ID, customer2.getId()));
    }

    //Prueba la correcta ejecución de borrar a un amigo convencido inexistente
    @Test
    public void testDeleteNotExistentConvincedFriend() {

        Customer customer = createCustomer("@darribo1501");

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteConvincedFriend(customer.getId(), NON_EXISTENT_ID));
    }

    //Prueba la correcta ejecución de borrar a un amigo convencido que no haya sido convencido por ese cliente
    @Test
    public void testDeleteConvincedFriendFromANotConvincingFriend() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");
        Customer customer2 = customerService.addCustomer("@pablitorios", customer.getId());
        Customer customer3 = createCustomer("@dati1609");
        Customer customer4 = customerService.addCustomer("@xabireymendez", customer2.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteConvincedFriend(customer.getId(), customer4.getId()));

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteConvincedFriend(customer3.getId(), customer.getId()));

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteConvincedFriend(null, customer.getId()));
    }

    @Test
    public void testAddResponsibleAndGetResponsible() throws InstanceNotFoundException {
        Customer customer = createCustomer("@darribo1501");
        Responsible added = createResponsible("Ana", "López", "600123123", customer.getId());
        Responsible found = customerService.getResponsible(added.getId());

        assertEquals(added.getId(), found.getId());
        assertEquals("Ana", found.getName());
        assertEquals("López", found.getSurname());
        assertEquals("600123123", found.getPhoneNumber());
    }

    @Test
    public void testGetResponsibleWithNonExistentId() {
        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getResponsible(NON_EXISTENT_ID));
    }

    @Test
    public void testGetResponsibleButIsOnlyCustomer() throws InstanceNotFoundException {
        Customer customer = customerService.addCustomer("@cliente", null);
        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getResponsible(customer.getId()));
    }


    @Test
    public void testUpdateResponsible() throws InstanceNotFoundException {
        Customer customer = createCustomer("@darribo1501");
        Responsible responsible = createResponsible("Ana", "López", "600123123", customer.getId());

        Responsible updated = customerService.updateResponsible(
                responsible.getId(), "Lucía", "Gómez", "600999999");

        assertEquals(responsible.getId(), updated.getId());
        assertEquals("Lucía", updated.getName());
        assertEquals("Gómez", updated.getSurname());
        assertEquals("600999999", updated.getPhoneNumber());
    }

    @Test
    public void testDeleteResponsible() throws InstanceNotFoundException {

        Customer customer = createCustomer("@darribo1501");
        Responsible responsible = createResponsible("Ana", "López", "600123123", customer.getId());

        customerService.deleteResponsible(responsible.getId());

        assertThrows(InstanceNotFoundException.class, () ->
                customerService.getResponsible(responsible.getId()));
    }

    @Test
    public void testDeleteNonExistentResponsible() {
        assertThrows(InstanceNotFoundException.class, () ->
                customerService.deleteResponsible(NON_EXISTENT_ID));
    }

}
